package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.content.PocContent;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.field.CropActions;
import dev.omega24.agricrops.core.field.CropState;
import dev.omega24.agricrops.core.field.SoilCell;
import dev.omega24.agricrops.core.field.RootZone;
import dev.omega24.agricrops.core.genetics.*;
import dev.omega24.agricrops.core.environment.*;
import java.util.*;
import dev.omega24.agricrops.persistence.CropNbt;
import dev.omega24.agricrops.persistence.FieldSoils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public final class CropTile extends TileEntity implements ITickable {
    public static final String DATA_KEY = "agricrops";
    private CropState crop;
    private NBTBase rejected;
    private boolean crossing, backcross;
    private long lastPropagation = Long.MIN_VALUE / 2;
    private long waterScanAt = Long.MIN_VALUE / 2;
    private boolean waterNearby;
    private String visual = "wheat";

    public CropState crop() { return crop; }
    public boolean quarantined() { return rejected != null; }
    public boolean crossing() { return crossing; }
    public boolean backcross() { return backcross; }
    public String visual() { return visual; }
    public boolean enableCrossing(boolean recovery) {
        if (crop != null || rejected != null) return false;
        crossing = true; backcross = recovery; markDirty(); return true;
    }

    @Override public void update() {
        if (!world.isRemote && Math.floorMod(world.getTotalWorldTime(), 20) == Math.floorMod(pos.toLong(), 20)) {
            advanceInterval();
        }
    }

    /** One normal loaded-world interval; also used by the explicit administrator acceptance fixture. */
    public void advanceInterval() {
        if (world.isRemote || rejected != null) return;
        RootZone roots = RootZones.tick(world, pos, nearbyWater() ? 8 : 0);
        if (crop != null) {
            CropDefinition definition = CropDefinitions.get(crop.crop());
            if (definition == null) return;
            CropActions.Step step = CropActions.step(definition, crop, roots, WorldEnvironment.sample(world, pos), CropDefinitions.all(), PocContent.traits());
            crop = step.crop; roots = step.roots;
            markDirty();
        }
        RootZones.write(world, pos, roots);
        if (crop == null && crossing) tryBreed();
        syncVisual();
    }

    public SoilCell soil() {
        String block = world.getBlockState(pos.down()).getBlock().getRegistryName().toString();
        return FieldSoils.get(world).observe(pos.down(), block);
    }

    public void addResources(int water, int nutrients) {
        SoilCell current = soil(); dev.omega24.agricrops.core.field.SoilProfile profile = PocContent.soils().get(current.block());
        if (profile != null) FieldSoils.get(world).put(pos.down(), current.add(Math.min(water, Math.max(0, profile.waterCapacity - current.water())), Math.min(nutrients, Math.max(0, profile.nutrientCapacity - current.nutrients()))));
    }
    public boolean compost() {
        if (soil().organic() >= 10000) return false;
        FieldSoils.get(world).put(pos.down(), soil().compost(2000)); return true;
    }

    private boolean nearbyWater() {
        if (world.getTotalWorldTime() - waterScanAt < 100) return waterNearby;
        waterScanAt = world.getTotalWorldTime(); waterNearby = false;
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            for (int dy = -1; dy <= 0; dy++) {
                BlockPos check = pos.add(dx, dy, dz);
                if (world.isBlockLoaded(check) && world.getBlockState(check).getMaterial() == net.minecraft.block.material.Material.WATER) {
                    waterNearby = true; return true;
                }
            }
        }
        return false;
    }

    public void plant(CropState specimen) {
        if (crop != null || rejected != null) throw new IllegalStateException("Crop sticks already occupied");
        PocContent.validate(specimen.specimen(), CropDefinitions.all());
        CropDefinition definition = CropDefinitions.get(specimen.crop());
        crop = CropState.plant(specimen.specimen(), specimen.origin() == SeedOrigin.CLIPPING ? definition.rules().rootingTicks : 0);
        crossing = false; backcross = false; markDirty(); syncVisual();
    }

    public int harvest() {
        return harvestProducts().values().stream().mapToInt(Integer::intValue).sum();
    }
    public Map<String, Integer> harvestProducts() {
        if (crop == null || rejected != null) return Collections.emptyMap();
        CropActions.Harvest result = CropActions.harvest(CropDefinitions.get(crop.crop()), crop);
        if (result.count > 0) { crop = result.crop; markDirty(); syncVisual(); }
        return result.products;
    }

    private boolean readyToPropagate() { return crop != null && !quarantined() && crop.rooting() == 0 && world.getTotalWorldTime() - lastPropagation >= 200; }
    private boolean canPay(int multiplier) {
        CropDefinition def = CropDefinitions.get(crop.crop()); RootZone roots = RootZones.read(world, pos);
        Phenotype phenotype = Phenotype.evaluate(def, crop, roots, WorldEnvironment.sample(world, pos), PocContent.traits());
        return phenotype.rate > 0 && !phenotype.hardBlocked && roots.availableWater(phenotype.reach) >= def.rules().reproductionWater * multiplier
                && roots.availableNutrients(phenotype.reach) >= def.rules().reproductionNutrients * multiplier;
    }
    private void payPropagation(int multiplier) {
        CropDefinition def = CropDefinitions.get(crop.crop()); RootZone roots = RootZones.read(world, pos);
        Phenotype phenotype = Phenotype.evaluate(def, crop, roots, WorldEnvironment.sample(world, pos), PocContent.traits());
        RootZones.write(world, pos, roots.consume(def.rules().reproductionWater * multiplier, def.rules().reproductionNutrients * multiplier, phenotype.reach));
        crop = crop.at(Math.max(0, crop.progress() - 20)); lastPropagation = world.getTotalWorldTime(); markDirty(); syncVisual();
    }
    public Specimen clip() {
        if (!readyToPropagate()) return null;
        CropDefinition def = CropDefinitions.get(crop.crop());
        dev.omega24.agricrops.core.growth.GrowthPhase phase = def.timeline().phaseAt(crop.progress());
        if (!def.rules().clippable || phase != dev.omega24.agricrops.core.growth.GrowthPhase.VEGETATIVE || !canPay(1)) return null;
        Specimen clipping = crop.specimen().clipping(); payPropagation(1); return clipping;
    }
    /** Canonical adjacent pair; one pair supplies all genes, alleles, composition and costs. */
    public boolean tryBreed() {
        if (world.isRemote || crop != null || !crossing || quarantined()) return false;
        List<CropTile> candidates = new ArrayList<>();
        for (net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.HORIZONTALS) {
            BlockPos parent = pos.offset(facing); if (!world.isBlockLoaded(parent)) continue;
            TileEntity tile = world.getTileEntity(parent); if (tile instanceof CropTile && ((CropTile) tile).readyToPropagate()) candidates.add((CropTile) tile);
        }
        int multiplier = backcross ? 2 : 1;
        for (CropTile donor : candidates) for (CropTile recipient : candidates) {
            if (donor == recipient || !donor.canPay(multiplier) || !recipient.canPay(multiplier)) continue;
            try {
                Specimen child = Breeding.cross(donor.crop, recipient.crop, backcross, CropDefinitions.all(), PocContent.traits(), PocContent.unlocks(), world.rand, world.rand.nextLong());
                // Child foundations must exist at this destination, not just at either parent.
                CropState planted = CropState.plant(child, 0);
                CropActions.Reason reason = CropActions.step(CropDefinitions.get(child.body), planted, RootZones.read(world, pos), WorldEnvironment.sample(world, pos), CropDefinitions.all(), PocContent.traits()).reason;
                if (reason == CropActions.Reason.SOIL || reason == CropActions.Reason.FOUNDATION) continue;
                donor.payPropagation(multiplier); recipient.payPropagation(multiplier); plant(planted); return true;
            } catch (IllegalArgumentException ignored) { /* An incompatible pair is not a corrupt world. */ }
        }
        return false;
    }

    public ITextComponent inspection() {
        if (rejected != null) return new TextComponentTranslation("agricrops.inspect.quarantined")
                .setStyle(new net.minecraft.util.text.Style().setColor(TextFormatting.RED));
        SoilCell soil = soil();
        if (crop == null) return new TextComponentTranslation(crossing ? (backcross ? "agricrops.inspect.backcross" : "agricrops.inspect.crossing") : "agricrops.inspect.empty", soil.water() / 100, soil.nutrients() / 100);
        CropDefinition definition = CropDefinitions.get(crop.crop());
        if (definition == null) return new TextComponentTranslation("agricrops.inspect.quarantined");
        CropActions.Reason reason = CropActions.step(definition, crop, RootZones.read(world, pos), WorldEnvironment.sample(world, pos), CropDefinitions.all(), PocContent.traits()).reason;
        int percent = (int) (100 * Math.min(crop.progress(), definition.timeline().totalTicks()) / definition.timeline().totalTicks());
        return new TextComponentTranslation("agricrops.inspect.crop", new TextComponentTranslation(definition.nameKey()),
                new TextComponentTranslation("agricrops.phase." + definition.timeline().phaseAt(crop.progress()).name().toLowerCase(java.util.Locale.ROOT)),
                percent, soil.water() / 100, soil.nutrients() / 100,
                new TextComponentTranslation("agricrops.reason." + reason.name().toLowerCase(java.util.Locale.ROOT)))
                .setStyle(new net.minecraft.util.text.Style().setColor(reason == CropActions.Reason.MATURE ? TextFormatting.GREEN
                        : reason == CropActions.Reason.GROWING || reason == CropActions.Reason.ROOTING ? TextFormatting.GRAY : TextFormatting.GOLD));
    }

    private void syncVisual() {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof CropSticksBlock)) return;
        int age = 0; String previousVisual = visual;
        if (crop != null) {
            CropDefinition definition = CropDefinitions.get(crop.crop());
            if (definition != null) { age = definition.stageAt(crop.progress()); visual = definition.rules().visual; }
        }
        IBlockState next = state.withProperty(CropSticksBlock.PLANTED, crop != null).withProperty(CropSticksBlock.AGE, age);
        if (!next.equals(state)) world.setBlockState(pos, next, 2);
        if (!next.equals(state) || !previousVisual.equals(visual)) world.notifyBlockUpdate(pos, state, next, 2);
    }

    @Override public boolean shouldRefresh(net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public NBTBase data() {
        if (rejected != null) return rejected.copy();
        NBTTagCompound data = new NBTTagCompound(); data.setInteger("schema", 2);
        data.setBoolean("crossing", crossing); data.setBoolean("backcross", backcross); data.setLong("lastPropagation", lastPropagation);
        if (crop != null) data.setTag("specimen", CropNbt.write(crop));
        return data;
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setTag(DATA_KEY, data()); return tag;
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); crop = null; rejected = null; crossing = false; backcross = false;
        if (!tag.hasKey(DATA_KEY)) return;
        if (!tag.hasKey(DATA_KEY, 10)) { rejected = tag.getTag(DATA_KEY).copy(); return; }
        NBTTagCompound data = tag.getCompoundTag(DATA_KEY);
        try {
            int schema = data.getInteger("schema");
            Set<String> allowed = new HashSet<>(Arrays.asList("schema", "specimen"));
            if (schema == 2) allowed.addAll(Arrays.asList("crossing", "backcross", "lastPropagation"));
            if (!data.hasKey("schema", 3) || (schema != 1 && schema != 2)
                    || !allowed.containsAll(data.getKeySet())) {
                throw new IllegalArgumentException("Unknown crop tile schema");
            }
            if (schema == 2) {
                if (!data.hasKey("crossing", 1) || !data.hasKey("backcross", 1) || !data.hasKey("lastPropagation", 4)) throw new IllegalArgumentException("Incomplete plot state");
                crossing = data.getBoolean("crossing"); backcross = data.getBoolean("backcross"); lastPropagation = data.getLong("lastPropagation");
            }
            if (data.hasKey("specimen")) {
                if (!data.hasKey("specimen", 10)) throw new IllegalArgumentException("Invalid specimen payload");
                crop = CropNbt.read(data.getCompoundTag("specimen"));
                if (CropDefinitions.get(crop.crop()) == null) throw new IllegalArgumentException("Removed crop definition");
                PocContent.validate(crop.specimen(), CropDefinitions.all()); visual = CropDefinitions.get(crop.crop()).rules().visual;
            }
        } catch (RuntimeException exception) {
            crop = null; rejected = data.copy();
        }
    }

    // Rendering uses only blockstate age/occupancy; genomes are not needed in chunk update packets.
    @Override public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.writeToNBT(new NBTTagCompound()); tag.setString("visual", visual); return tag;
    }
    @Override public void handleUpdateTag(NBTTagCompound tag) { visual = tag.getString("visual"); }
    @Override public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity packet) {
        handleUpdateTag(packet.getNbtCompound()); world.markBlockRangeForRenderUpdate(pos, pos);
    }
}
