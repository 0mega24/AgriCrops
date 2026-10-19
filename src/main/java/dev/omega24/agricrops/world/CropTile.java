package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.field.CropActions;
import dev.omega24.agricrops.core.field.CropState;
import dev.omega24.agricrops.core.field.SoilCell;
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

    public CropState crop() { return crop; }
    public boolean quarantined() { return rejected != null; }

    @Override public void update() {
        if (!world.isRemote && Math.floorMod(world.getTotalWorldTime(), 20) == Math.floorMod(pos.toLong(), 20)) {
            advanceInterval();
        }
    }

    /** One normal loaded-world interval; also used by the explicit administrator acceptance fixture. */
    public void advanceInterval() {
        if (world.isRemote || rejected != null) return;
        SoilCell soil = soil();
        if (nearbyWater()) soil = soil.add(100, 0);
        if (crop != null) {
            CropDefinition definition = CropDefinitions.get(crop.crop());
            if (definition == null) return;
            CropActions.Step step = CropActions.step(definition, crop, soil, world.getLightFromNeighbors(pos.up()));
            crop = step.crop; soil = step.soil;
            markDirty();
        }
        FieldSoils.get(world).put(pos.down(), soil);
        syncVisual();
    }

    public SoilCell soil() {
        String block = world.getBlockState(pos.down()).getBlock().getRegistryName().toString();
        return FieldSoils.get(world).observe(pos.down(), block);
    }

    public void addResources(int water, int nutrients) {
        FieldSoils.get(world).put(pos.down(), soil().add(water, nutrients));
    }

    private boolean nearbyWater() {
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            for (int dy = -1; dy <= 0; dy++) {
                BlockPos check = pos.add(dx, dy, dz);
                if (world.isBlockLoaded(check) && world.getBlockState(check).getMaterial() == net.minecraft.block.material.Material.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    public void plant(CropState specimen) {
        if (crop != null || rejected != null) throw new IllegalStateException("Crop sticks already occupied");
        crop = specimen.at(0); markDirty(); syncVisual();
    }

    public int harvest() {
        if (crop == null || rejected != null) return 0;
        CropActions.Harvest result = CropActions.harvest(CropDefinitions.get(crop.crop()), crop);
        if (result.count > 0) { crop = result.crop; markDirty(); syncVisual(); }
        return result.count;
    }

    public ITextComponent inspection() {
        if (rejected != null) return new TextComponentTranslation("agricrops.inspect.quarantined")
                .setStyle(new net.minecraft.util.text.Style().setColor(TextFormatting.RED));
        SoilCell soil = soil();
        if (crop == null) return new TextComponentTranslation("agricrops.inspect.empty", soil.water() / 100, soil.nutrients() / 100);
        CropDefinition definition = CropDefinitions.get(crop.crop());
        if (definition == null) return new TextComponentTranslation("agricrops.inspect.quarantined");
        CropActions.Reason reason = CropActions.step(definition, crop, soil, world.getLightFromNeighbors(pos.up())).reason;
        int percent = (int) (100 * Math.min(crop.progress(), definition.timeline().totalTicks()) / definition.timeline().totalTicks());
        return new TextComponentTranslation("agricrops.inspect.crop", new TextComponentTranslation(definition.nameKey()),
                new TextComponentTranslation("agricrops.phase." + definition.timeline().phaseAt(crop.progress()).name().toLowerCase(java.util.Locale.ROOT)),
                percent, soil.water() / 100, soil.nutrients() / 100,
                new TextComponentTranslation("agricrops.reason." + reason.name().toLowerCase(java.util.Locale.ROOT)));
    }

    private void syncVisual() {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof CropSticksBlock)) return;
        int age = 0;
        if (crop != null) {
            CropDefinition definition = CropDefinitions.get(crop.crop());
            if (definition != null) age = crop.progress() >= definition.timeline().totalTicks() ? 7
                    : (int) (crop.progress() * 7 / definition.timeline().totalTicks());
        }
        IBlockState next = state.withProperty(CropSticksBlock.PLANTED, crop != null).withProperty(CropSticksBlock.AGE, age);
        if (!next.equals(state)) world.setBlockState(pos, next, 2);
    }

    @Override public boolean shouldRefresh(net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public NBTBase data() {
        if (rejected != null) return rejected.copy();
        NBTTagCompound data = new NBTTagCompound(); data.setInteger("schema", 1);
        if (crop != null) data.setTag("specimen", CropNbt.write(crop));
        return data;
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setTag(DATA_KEY, data()); return tag;
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); crop = null; rejected = null;
        if (!tag.hasKey(DATA_KEY)) return;
        if (!tag.hasKey(DATA_KEY, 10)) { rejected = tag.getTag(DATA_KEY).copy(); return; }
        NBTTagCompound data = tag.getCompoundTag(DATA_KEY);
        try {
            if (!data.hasKey("schema", 3) || data.getInteger("schema") != 1
                    || data.getKeySet().stream().anyMatch(key -> !key.equals("schema") && !key.equals("specimen"))) {
                throw new IllegalArgumentException("Unknown crop tile schema");
            }
            if (data.hasKey("specimen")) {
                if (!data.hasKey("specimen", 10)) throw new IllegalArgumentException("Invalid specimen payload");
                crop = CropNbt.read(data.getCompoundTag("specimen"));
                if (CropDefinitions.get(crop.crop()) == null) throw new IllegalArgumentException("Removed crop definition");
            }
        } catch (RuntimeException exception) {
            crop = null; rejected = data.copy();
        }
    }

    // Rendering uses only blockstate age/occupancy; genomes are not needed in chunk update packets.
    @Override public NBTTagCompound getUpdateTag() { return super.writeToNBT(new NBTTagCompound()); }
}
