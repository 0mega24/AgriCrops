package dev.omega24.agricrops.tiles;

import com.agricraft.agricore.core.AgriCore;
import com.agricraft.agricore.util.MathHelper;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.crop.IAgriCrop;
import dev.omega24.agricrops.api.v1.fertilizer.IAgriFertilizer;
import dev.omega24.agricrops.api.v1.misc.IAgriDisplayable;
import dev.omega24.agricrops.api.v1.misc.IAgriPeripheralMethod;
import dev.omega24.agricrops.api.v1.plant.CropLootTier;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.api.v1.util.MethodResult;
import dev.omega24.agricrops.core.WeedPlant;
import dev.omega24.agricrops.farming.CropStatCalculator;
import dev.omega24.agricrops.farming.PlantStats;
import dev.omega24.agricrops.farming.soil.SoilColumn;
import dev.omega24.agricrops.farming.soil.SoilColumnHelper;
import dev.omega24.agricrops.init.AgriItems;
import dev.omega24.agricrops.reference.AgriCraftConfig;
import dev.omega24.agricrops.reference.AgriNBT;
import dev.omega24.agricrops.reference.Constants;
import com.infinityraider.infinitylib.block.tile.TileEntityBase;
import com.infinityraider.infinitylib.utility.MessageUtil;
import com.infinityraider.infinitylib.utility.WorldHelper;
import com.infinityraider.infinitylib.utility.debug.IDebuggable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SimpleComponent;
import li.cil.oc.api.network.Visibility;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@net.minecraftforge.fml.common.Optional.Interface(modid = "opencomputers", iface = "li.cil.oc.api.network.Environment")
@net.minecraftforge.fml.common.Optional.Interface(modid = "opencomputers", iface = "li.cil.oc.api.network.SimpleComponent")
@net.minecraftforge.fml.common.Optional.Interface(modid = "opencomputers", iface = "li.cil.oc.api.network.ManagedPeripheral")
public class TileEntityCrop extends TileEntityBase implements IAgriCrop, IDebuggable, IAgriDisplayable,
        Environment, SimpleComponent, ManagedPeripheral {

    /**
     * Nutrients drained from the soil column per successful growth tick. Placeholder
     * balance number, like the rest of the growth math it feeds into.
     */
    private static final int GROWTH_TICK_NUTRIENT_DRAIN = 1;

    /**
     * Moisture added to the soil column, per growth tick, when a water source is found
     * nearby (crop-mod-design.md §4/§11 step 3). Placeholder balance number.
     */
    private static final int MOISTURE_TOPUP_AMOUNT = 25;

    /**
     * Base moisture decay per growth tick, before the soil column's moisture-retention rate
     * softens it. Placeholder balance number.
     */
    private static final int MOISTURE_DECAY_AMOUNT = 2;

    private AgriSeed seed;
    private int growthStage;
    private boolean crossCrop = false;

    /**
     * Whether this crop is in the destabilizing telegraph window (crop-mod-design.md §8):
     * risky, but still curable by right-clicking it before {@link #destabilizeExpiry}.
     */
    private boolean destabilizing = false;

    /**
     * The {@link World#getTotalWorldTime()} value at which an uncured {@link #destabilizing}
     * crop fully converts into a weed.
     */
    private long destabilizeExpiry = 0L;

    /**
     * Whether an attached fertilizer/irrigation device should act on this crop
     * (crop-mod-design.md §9's {@code setAutoFertilize}/{@code setAutoWater}) - gates an
     * external device, never the crop stick's own moisture/nutrient ticking directly.
     */
    private boolean autoFertilize = false;
    private boolean autoWater = false;

    /**
     * This crop stick's OC network node, stored as {@code Object} (rather than {@link Node})
     * so the field's declared type doesn't require {@code li.cil.oc} classes to be loadable
     * when OpenComputers isn't installed - only code inside {@code @Optional.Method}-annotated
     * methods touches the real type.
     */
    private Object ocNode;

    // =========================================================================
    // Event Methods
    // <editor-fold>
    // =========================================================================
    @Override
    @Nonnull
    public MethodResult onGrowthTick() {
        // If remote world, pass.
        if (this.isRemote()) {
            return MethodResult.PASS;
        }

        // Water/decay the soil column every growth tick, independent of what else happens
        // this tick (mirrors vanilla farmland's own per-random-tick moisture check).
        this.tickMoisture();

        // Roll/advance weed risk every growth tick too, independent of the rest of this
        // method (crop-mod-design.md §8: "each growth tick, compute weedRisk...").
        this.tickWeedRisk();

        // Otherwise attempt to do something with the event.
        if (this.isCrossCrop() && AgriCraftConfig.crossOverChance > this.getRandom().nextDouble()) {
            this.crossOver();
            return MethodResult.SUCCESS;
        } else if (!this.hasSeed()) {
            this.spawn();
            return MethodResult.SUCCESS;
        } else if (this.isMature()) {
            this.spread();
            return MethodResult.SUCCESS;
        } else if (CropStatCalculator.growthTickChance(this.seed.getPlant(), this.seed.getStat(),
                this.nutrientSatisfaction(), this.moistureSatisfaction(), this.habitatSatisfaction()) > this.getRandom().nextDouble()) {
            SoilColumnHelper.getOrCreateColumn(this.getWorld(), this.pos.down())
                    .ifPresent(column -> column.drainNutrients(GROWTH_TICK_NUTRIENT_DRAIN));
            this.applyGrowthTick();
            return MethodResult.SUCCESS;
        }

        // The method has failed.
        return MethodResult.FAIL;
    }

    @Override
    @Nonnull
    public MethodResult onApplySeeds(@Nonnull AgriSeed seed, @Nullable EntityPlayer player) {
        // Ensure seed is valid.
        Preconditions.checkNotNull(seed, "`7Cannot apply a null seed!`r");

        // If on client side do nothing!
        if (this.isRemote()) {
            return MethodResult.PASS;
        }

        // If this is a cross-crop or has a plant, do nothing!
        if (this.isCrossCrop() || this.hasSeed()) {
            return MethodResult.FAIL;
        }

        // If the soil is wrong, report and abort.
        if (!seed.getPlant().getGrowthRequirement().hasValidSoil(this.getWorld(), pos)) {
            MessageUtil.messagePlayer(player, "`7The soil is not valid for this seed. You can't plant it here.`r");
            return MethodResult.FAIL;
        }

        // If the additional conditions are wrong, warn and continue.
        if (!seed.getPlant().getGrowthRequirement().hasValidConditions(this.getWorld(), pos)) {
            MessageUtil.messagePlayer(player, "`7Caution: This plant has additional requirements that are unmet.`r");
        }

        // If the lighting is wrong, warn and continue.
        if (!seed.getPlant().getGrowthRequirement().hasValidLight(this.getWorld(), pos)) {
            MessageUtil.messagePlayer(player, "`7Caution: This plant won't grow with the current light level.`r");
        }

//        // Notify event listeners of a planting event.
//        if (!MinecraftForge.EVENT_BUS.post(new CropPlantedEvent(this, seed.get(), player))) {
//            // The event was aborted! Abort!
//            return false;
//        }
        // Actually plant the seed.
        this.setSeed(seed);

        // The method was a success!
        return MethodResult.SUCCESS;
    }

    @Override
    @Nonnull
    public MethodResult onApplyCrops(@Nullable EntityPlayer player) {
        // If on client side do nothing!
        if (this.isRemote()) {
            return MethodResult.PASS;
        }

        // If this is a cross-crop or has a plant, do nothing!
        if (this.isCrossCrop() || this.hasSeed()) {
            return MethodResult.FAIL;
        }

        // Set the crop to be a cross-crop.
        final boolean wasSet = this.setCrossCrop(true);

        // Notify of success.
        return wasSet ? MethodResult.SUCCESS : MethodResult.FAIL;
    }

    @Override
    @Nonnull
    public MethodResult onBroken(@Nonnull Consumer<ItemStack> consumer, @Nullable EntityPlayer player) {
        // Verify the consumer is not null.
        Preconditions.checkNotNull(consumer);

        // If on client side do nothing!
        if (this.isRemote()) {
            return MethodResult.PASS;
        }

        // Drop drops if should drop.
        if (player == null || !player.isCreative()) {
            this.getDrops(consumer, true, true, true);
        }

        // Notify listeners this crop's plant is gone, before the tile is actually removed.
        if (this.hasSeed()) {
            this.pushSignal("crop_died");
        }

        // Remove the block.
        this.getWorld().removeTileEntity(pos);
        this.getWorld().setBlockToAir(pos);

        // The operation was a success.
        return MethodResult.SUCCESS;
    }

    // =========================================================================
    // Event Methods
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // Misc. Methods
    // <editor-fold>
    // =========================================================================
    public void getDrops(@Nonnull Consumer<ItemStack> consumer, boolean includeCropSticks, boolean includeSeeds, boolean includeProducts) {
        // Check that the consumer is not null.
        Preconditions.checkNotNull(consumer);

        // Perform crop stick drop.
        if (includeCropSticks) {
            consumer.accept(new ItemStack(AgriItems.getInstance().CROPS, this.isCrossCrop() ? 2 : 1));
        }
        if (this.hasSeed()) {
            if (includeSeeds && this.seed.getPlant().getSeedDropChanceBase() + this.growthStage * this.seed.getPlant().getSeedDropChanceBonus() > this.getRandom().nextDouble()) {
                consumer.accept(this.getSeed().toStack());
            }
            if (includeProducts) {
                this.findLootTier().ifPresent(tier -> {
                    for (int trials = CropStatCalculator.harvestTrials(this.seed.getStat()); trials > 0; trials--) {
                        tier.roll(consumer, this, this.seed.getStat(), this.getRandom());
                    }
                });
            }
        }
    }

    /**
     * The highest-{@code minStage} loot tier (crop-mod-design.md §6) the current growth
     * stage qualifies for, or empty if the plant hasn't reached even the lowest tier yet.
     */
    private Optional<CropLootTier> findLootTier() {
        if (!this.hasSeed()) {
            return Optional.empty();
        }
        CropLootTier best = null;
        for (CropLootTier tier : this.seed.getPlant().getLootTiers()) {
            if (tier.getMinStage() <= this.growthStage && (best == null || tier.getMinStage() > best.getMinStage())) {
                best = tier;
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Public view of {@link #findLootTier()}'s minimum stage, for OC telemetry
     * (crop-mod-design.md §9's {@code getGrowthProgress()} - "which loot tier that stage
     * currently qualifies for").
     */
    public Optional<Integer> getCurrentLootTierMinStage() {
        return this.findLootTier().map(CropLootTier::getMinStage);
    }

    @Override
    public boolean canBeHarvested() {
        return this.findLootTier().isPresent();
    }

    // =========================================================================
    // Misc. Methods
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // ISeedProvider Methods
    // <editor-fold>
    // =========================================================================
    @Override
    public boolean hasSeed() {
        return (this.seed != null);
    }

    @Override
    public AgriSeed getSeed() {
        return this.seed;
    }

    @Override
    public boolean acceptsSeed(@Nullable AgriSeed seed) {
        return (!this.crossCrop) && (this.seed == null || seed == null);
    }

    /**
     * This sets the seed for this crop. Using null will remove the seed. If
     * this call changes the value for seed: - It will first reset the growth
     * stage back to zero. - Then it will make sure that markForUpdate gets
     * called, if setGrowthStage didn't do it already. - And then it will return
     * true. Otherwise it will return false, indicating there was no change and
     * no update.
     *
     * @param seed the seed to associate with this instance.
     * @return true if this changed the seed and caused an update.
     */
    @Override
    public boolean setSeed(@Nullable AgriSeed seed) {
        // Check if the new value is already equal to the current seed. (I.e. same plant and same stats.)
        if (Objects.equal(this.seed, seed)) {
            // No change to make.
            return false;
        }

        // Otherwise set the seed to the new value.
        this.seed = seed;

        // Reset the growth stage.
        this.growthStage = 0;

        // Mark the tile for an update.
        this.markForUpdate();

        // Finally report that a change and an update did occur.
        return true;
    }

    // =========================================================================
    // ISeedProvider Methods
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // IAgriCrop Methods
    // -------------------------------------------------------------------------
    // The following methods were added due to a ForgeGradle obfuscation bug.
    // See: https://github.com/MinecraftForge/ForgeGradle/issues/205
    // <editor-fold>
    // =========================================================================
    @Override
    public BlockPos getCropPos() {
        return this.getPos();
    }

    @Override
    public World getCropWorld() {
        return this.getWorld();
    }

    // =========================================================================
    // IAgriCrop Methods
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // Misc.
    // =========================================================================
    @Override
    public boolean isCrossCrop() {
        return crossCrop;
    }

    @Override
    public boolean setCrossCrop(boolean status) {
        // If remote, change was failure.
        if (this.isRemote()) {
            return false;
        }

        // If we have a plant, change was a failure.
        if (this.hasSeed()) {
            return false;
        }

        // If the new state does not differ, change was a failure.
        if (this.crossCrop == status) {
            return false;
        }

        // Otherwise perform change.
        this.crossCrop = status;
        SoundType type = Blocks.PLANKS.getSoundType(null, null, null, null);
        this.getWorld().playSound(null, (double) ((float) xCoord() + 0.5F), (double) ((float) yCoord() + 0.5F), (double) ((float) zCoord() + 0.5F), type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
        this.markForUpdate();

        // Operation was a success!
        return true;
    }

    @Override
    public int getGrowthStage() {
        return this.growthStage;
    }

    @Override
    public boolean setGrowthStage(int stage) {
        // If remote, abort! No change to report.
        if (this.isRemote()) {
            return false;
        }

        // Make sure that the new value is valid.
        if (this.hasSeed()) {
            // If there is a plant currently, make sure the value is valid.
            stage = MathHelper.inRange(stage, 0, this.seed.getPlant().getGrowthStages() - 1);
        } else if (stage != 0) {
            // If this has no plant, the growth stage should be zero.
            // And if someone was trying to set it as non-zero, then that's a mistake.
            AgriCore.getCoreLogger().warn("Can't set a non-zero growth stage when a crop has no seed!");
            stage = 0;
        }

        // If the new growth stage is different, perform update.
        if (stage != this.growthStage) {
            this.growthStage = stage;
            this.markForUpdate();
            this.pushSignal("crop_growth", stage);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isFertile(@Nullable IAgriPlant plant) {
        return (plant != null)
                && this.getWorld().isAirBlock(this.pos.up())
                && plant.getGrowthRequirement().isMet(this.getWorld(), pos);
    }

    @SideOnly(Side.CLIENT)
    public float getCropHeight() {
        return this.hasSeed() ? this.getSeed().getPlant().getHeight(getBlockMetadata()) : Constants.UNIT * 13;
    }

    @Override
    public boolean isMature() {
        return this.hasSeed() && (this.getGrowthStage() + 1 >= this.getSeed().getPlant().getGrowthStages());
    }

    @Override
    @Nonnull
    public Optional<ISoilProfile> getSoilProfile() {
        final IBlockState state = this.getWorld().getBlockState(this.pos.down());
        return AgriApi.getSoilProfileRegistry().get(state);
    }

    /**
     * The soil column's current nutrients as a fraction of its capacity, or 1.0 if there's
     * no column to read yet (soil not yet touched by a growth tick or fertilizer).
     */
    private double nutrientSatisfaction() {
        return SoilColumnHelper.getOrCreateColumn(this.getWorld(), this.pos.down())
                .filter(column -> column.getNutrientCapacity() > 0)
                .map(column -> (double) column.getNutrients() / column.getNutrientCapacity())
                .orElse(1.0);
    }

    /**
     * The soil column's current moisture as a fraction of {@link SoilColumn#MAX_MOISTURE},
     * or the neutral baseline (0.5) if there's no column to read yet.
     */
    private double moistureSatisfaction() {
        return SoilColumnHelper.getOrCreateColumn(this.getWorld(), this.pos.down())
                .map(column -> (double) column.getMoisture() / SoilColumn.MAX_MOISTURE)
                .orElse(0.5);
    }

    /**
     * How far outside the plant's optimal elevation band (crop-mod-design.md §5) growth
     * chance keeps tapering down before {@link CropStatCalculator}'s Resilience-softened floor
     * takes over entirely. Placeholder balance number.
     */
    private static final double ELEVATION_FALLOFF = 24.0;

    /**
     * How well the crop's current biome and elevation match its plant's preferences (0..1,
     * average of the two independent satisfactions), per crop-mod-design.md §5. A plant with
     * no preference for either is always fully satisfied.
     */
    private double habitatSatisfaction() {
        final IAgriPlant plant = this.seed.getPlant();
        return (this.biomeSatisfaction(plant) + this.elevationSatisfaction(plant)) / 2.0;
    }

    /**
     * Public read-only view of {@link #habitatSatisfaction()} for OC telemetry
     * (crop-mod-design.md §9's {@code getBiomeElevationModifier()}) - 1.0 (fully satisfied)
     * for an empty crop stick, matching the no-preference default.
     */
    public double getBiomeElevationModifier() {
        return this.hasSeed() ? this.habitatSatisfaction() : 1.0;
    }

    /**
     * 1.0 if the plant has no preferred biome tags, or if the crop's current biome carries any
     * of them; 0.0 otherwise (softened later by Resilience in {@link CropStatCalculator}).
     */
    private double biomeSatisfaction(IAgriPlant plant) {
        final Collection<BiomeDictionary.Type> preferred = plant.getPreferredBiomeTypes();
        if (preferred.isEmpty()) {
            return 1.0;
        }
        final Biome biome = this.getWorld().getBiome(this.pos);
        final Set<BiomeDictionary.Type> actual = BiomeDictionary.getTypes(biome);
        return actual.stream().anyMatch(preferred::contains) ? 1.0 : 0.0;
    }

    /**
     * 1.0 inside the plant's optimal elevation band, tapering linearly to 0.0 at
     * {@link #ELEVATION_FALLOFF} blocks past the nearest edge of that band.
     */
    private double elevationSatisfaction(IAgriPlant plant) {
        final int y = this.pos.getY();
        final int min = plant.getOptimalElevationMin();
        final int max = plant.getOptimalElevationMax();
        if (y >= min && y <= max) {
            return 1.0;
        }
        final int distance = y < min ? min - y : y - max;
        return Math.max(0.0, 1.0 - distance / ELEVATION_FALLOFF);
    }

    /**
     * Tops up the soil column's moisture if a water source is nearby, then applies the
     * per-tick decay (softened by the soil's moisture retention). Runs every growth tick
     * regardless of what else happens that tick, per crop-mod-design.md §4.
     */
    private void tickMoisture() {
        final BlockPos soilPos = this.pos.down();
        SoilColumnHelper.getOrCreateColumn(this.getWorld(), soilPos).ifPresent(column -> {
            if (SoilColumnHelper.hasNearbyWaterSource(this.getWorld(), soilPos)) {
                column.addMoisture(MOISTURE_TOPUP_AMOUNT);
            }
            final float retention = this.getSoilProfile().map(ISoilProfile::getMoistureRetention).orElse(0.0f);
            column.decayMoisture(retention, MOISTURE_DECAY_AMOUNT);
        });
    }

    // =========================================================================
    // Weed risk (crop-mod-design.md §8)
    // =========================================================================
    /**
     * Whether this crop is currently in the destabilizing telegraph window.
     */
    public boolean isDestabilizing() {
        return this.destabilizing;
    }

    /**
     * Rolls weed risk (§8) against a stable plant, or checks a destabilizing one for expiry.
     * A weed itself (see {@link WeedPlant}) never rolls risk again - it's already fully
     * converted. Runs every growth tick regardless of what else that tick does, same shape as
     * {@link #tickMoisture()}.
     */
    private void tickWeedRisk() {
        if (!this.hasSeed() || this.isCrossCrop() || this.seed.getPlant().isWeed()) {
            return;
        }
        if (this.destabilizing) {
            if (this.getWorld().getTotalWorldTime() >= this.destabilizeExpiry) {
                this.convertToWeed();
            }
        } else if (CropStatCalculator.weedRisk(this.seed.getStat()) > this.getRandom().nextDouble()) {
            this.beginDestabilizing();
        }
    }

    /**
     * Enters the destabilizing telegraph state: a visible tell starts (client reads
     * {@link #isDestabilizing()} directly off the synced tile, see {@code RenderCrop}), and the
     * crop has {@link AgriCraftConfig#destabilizingWindowTicks} to be cured before it converts.
     */
    private void beginDestabilizing() {
        this.destabilizing = true;
        this.destabilizeExpiry = this.getWorld().getTotalWorldTime() + AgriCraftConfig.destabilizingWindowTicks;
        this.markForUpdate();
        this.onDestabilizingBegin();
    }

    /**
     * Fires whenever a crop begins destabilizing - pushes the OC {@code crop_destabilizing}
     * signal event (crop-mod-design.md §9, wired in §11 step 8).
     */
    private void onDestabilizingBegin() {
        this.pushSignal("crop_destabilizing");
    }

    /**
     * Cures a destabilizing crop back to stable, at the cost of knocking its Growth and
     * Mutation Chance down slightly (§8: "some cost is worth attaching here... so catching it
     * isn't entirely free"). Growth stage/progress is untouched - unlike a plant/species
     * change, curing isn't a new seed.
     *
     * @return true if this crop was destabilizing and is now cured.
     */
    public boolean cureDestabilizing() {
        if (this.isRemote() || !this.destabilizing) {
            return false;
        }
        this.destabilizing = false;
        final IAgriStat stat = this.seed.getStat();
        final IAgriStat cured = stat
                .with(CropStat.GROWTH, stat.get(CropStat.GROWTH) - AgriCraftConfig.destabilizeCureCost)
                .with(CropStat.MUTATION_CHANCE, stat.get(CropStat.MUTATION_CHANCE) - AgriCraftConfig.destabilizeCureCost);
        this.seed = this.seed.withStat(cured);
        this.markForUpdate();
        return true;
    }

    /**
     * Full conversion (§8): the destabilizing window expired uncaught, so this crop's plant
     * becomes an actual weed, carrying its stat genome over (weed spread strength is derived
     * from whatever Growth/Mutation Chance it converted from, per
     * {@link CropStatCalculator#weedOvertakes}). Reuses {@link #setSeed(AgriSeed)} so growth
     * stage resets - the weed has its own growth-stage progression, distinct from whatever
     * plant it replaced.
     */
    private void convertToWeed() {
        this.destabilizing = false;
        final IAgriStat carryOver = this.seed.getStat();
        this.pushSignal("crop_died");
        AgriApi.getPlantRegistry().get(WeedPlant.ID)
                .ifPresent(weed -> this.setSeed(new AgriSeed(weed, carryOver)));
    }

    // =========================================================================
    // IWeedable Methods
    // =========================================================================
    public boolean spawn() {
        // If in remote world, abort!
        if (this.isRemote()) {
            return false;
        }

        // If already have plant, abort!
        if (this.hasSeed()) {
            return false;
        }

        // Attempt to spawn plant.
        for (IAgriPlant p : AgriApi.getPlantRegistry().all()) {
            if (p.getSpawnChance() > this.getRandom().nextDouble() && this.isFertile(p)) {
                this.setCrossCrop(false);
                this.setSeed(new AgriSeed(p, new PlantStats()));
                return true;
            }
        }

        // The operation was a failure.
        return false;
    }

    public boolean spread() {
        // If remote, abort.
        if (this.isRemote()) {
            return false;
        }

        // If don't have plant, abort.
        if (this.seed == null) {
            return false;
        }

        final IAgriPlant plant = this.seed.getPlant();

        // Try to spread in each direction.
        for (IAgriCrop crop : WorldHelper.getTileNeighbors(this.getWorld(), pos, IAgriCrop.class)) {
            // Note: Checking the probability is faster. Also check that this plant can be planted there.
            if (plant.getSpreadChance() > this.getRandom().nextDouble()
                    && plant.getGrowthRequirement().hasValidSoil(this.getWorld(), crop.getCropPos())) {
                final AgriSeed other = crop.getSeed();
                if (other == null) {
                    if (!crop.isCrossCrop()) {
                        crop.setSeed(this.seed);
                        return true;
                    }
                } else if (canOvertake(this.seed, other, this.getRandom())) {
                    crop.setCrossCrop(false);
                    crop.setSeed(this.seed);
                    return true;
                }

            }
        }

        // The spreading failed.
        return false;
    }

    public static boolean canOvertake(@Nonnull AgriSeed overtaker, @Nonnull AgriSeed victim, @Nonnull Random rand) {
        if (!overtaker.getPlant().isAggressive()) {
            return false;
        }
        // A weed's spread contest is driven by the Growth/Mutation Chance it converted from
        // (crop-mod-design.md §8), not its own Resilience like a regular aggressive plant.
        return overtaker.getPlant().isWeed()
                ? CropStatCalculator.weedOvertakes(overtaker.getStat(), victim.getStat(), rand)
                : CropStatCalculator.overtakes(overtaker.getStat(), victim.getStat(), rand);
    }

    // =========================================================================
    // Auto-fertilize / auto-water gating (crop-mod-design.md §9)
    // =========================================================================
    /**
     * Whether an attached fertilizer device should act on this crop. This crop stick's own
     * moisture/nutrient ticking ({@link #tickMoisture()}, growth-tick nutrient drain) is
     * unaffected either way - this only gates an external device via OC's
     * {@code setAutoFertilize}.
     */
    public boolean isAutoFertilize() {
        return this.autoFertilize;
    }

    public void setAutoFertilize(boolean value) {
        this.autoFertilize = value;
        this.markForUpdate();
    }

    /**
     * Whether an attached irrigation device should act on this crop, via OC's
     * {@code setAutoWater}. See {@link #isAutoFertilize()}.
     */
    public boolean isAutoWater() {
        return this.autoWater;
    }

    public void setAutoWater(boolean value) {
        this.autoWater = value;
        this.markForUpdate();
    }

    // =========================================================================
    // IAgriFertilizable Methods
    // =========================================================================
    @Override
    public boolean acceptsFertilizer(@Nullable IAgriFertilizer fertilizer) {
        // If the fertilizer is null, then well, no.
        if (fertilizer == null) {
            return false;
        } // First check if this is a cross crops. In all other cases this is a regular crop.
        else if (this.crossCrop) {
            // This is a cross crop.
            // + A cross over (e.g. mutation) can occur if both requirements are met.
            // - Otherwise the fertilizer usage is blocked by returning false.
            return AgriCraftConfig.fertilizerMutation && fertilizer.canTriggerMutation();
        } else if (!this.hasSeed()) {
            // This is a regular crop, AND it is empty.
            // + Random plants (such as weeds) have a chance to spawn.
            return true;
        } else {
            // This is a regular crop, AND it has a plant, AND that plant allows boosts from fertilizers.
            // + Immature plants can grow.
            // + Fully grown plants have a chance to spread clones to neighboring crops
            // return true;
            // This is a regular crop, AND it has a plant, AND that plant does NOT allow fertilizers.
            // - Block the consumption and application of the fertilizer by returning false.
            // return false;
            return this.getSeed().getPlant().isFertilizable();
        }
    }

    @Override
    @Nonnull
    public MethodResult onApplyFertilizer(@Nullable IAgriFertilizer fertilizer, @Nonnull Random rand) {
        // Validate
        Preconditions.checkNotNull(rand);

        // Do stuff
        if (this.isRemote()) {
            // The client doesn't need to do anything special.
            return MethodResult.PASS;
        } else if (!this.acceptsFertilizer(fertilizer)) {
            // This method has been called in error. Make a note and block the tick.
            AgriCore.getCoreLogger().warn("onApplyFertilizer should not be called if acceptFertilizer is false!");
            return MethodResult.FAIL;
        } else {
            // The regular growth tick method handles the rest, now that the special cases have been filtered out.
            this.onGrowthTick();
            return MethodResult.SUCCESS;
        }
    }

    // =========================================================================
    // IHarvestable methods.
    // =========================================================================
    @Override
    @Nonnull
    public MethodResult onHarvest(@Nonnull Consumer<ItemStack> consumer, @Nullable EntityPlayer player) {
        // Check that consumer is not null.
        Preconditions.checkNotNull(consumer);

        // Skip harvest if remote.
        if (this.isRemote()) {
            return MethodResult.PASS;
        } else if (this.isCrossCrop()) {
            if (this.setCrossCrop(false)) {
                consumer.accept(new ItemStack(AgriItems.getInstance().CROPS, 1));
            }
            return MethodResult.SUCCESS;
        } else if (this.canBeHarvested()) {
            boolean resetsGrowth = this.findLootTier().map(CropLootTier::resetsGrowth).orElse(true);
            this.getDrops(consumer, false, false, true);
            this.pushSignal("crop_harvest");
            if (resetsGrowth) {
                this.setGrowthStage(0);
            }
            return MethodResult.SUCCESS;
        } else {
            return MethodResult.PASS;
        }
    }

    // =========================================================================
    // IRakeable methods.
    // =========================================================================
    @Override
    public boolean onRaked(@Nonnull Consumer<ItemStack> consumer, @Nullable EntityPlayer player) {
        // Check that consumer is valid.
        Preconditions.checkNotNull(consumer);
        // Actually do something.
        if (!this.isRemote() && this.canBeRaked()) {
            this.getDrops(consumer, false, AgriCraftConfig.enableRakingSeedDrops, AgriCraftConfig.enableRakingItemDrops);
            this.setSeed(null);
            return true;
        } else {
            return false;
        }
    }

    // =========================================================================
    // Other
    // =========================================================================
    @Override
    public void writeTileNBT(@Nonnull NBTTagCompound tag) {
        Preconditions.checkNotNull(tag);
        tag.setBoolean(AgriNBT.CROSS_CROP, crossCrop);
        tag.setInteger(AgriNBT.META, growthStage);
        tag.setBoolean(AgriNBT.DESTABILIZING, destabilizing);
        tag.setLong(AgriNBT.DESTABILIZE_EXPIRY, destabilizeExpiry);
        tag.setBoolean(AgriNBT.AUTO_FERTILIZE, autoFertilize);
        tag.setBoolean(AgriNBT.AUTO_WATER, autoWater);
        if (this.hasSeed()) {
            this.getSeed().getStat().writeToNBT(tag);
            tag.setString(AgriNBT.SEED, this.getSeed().getPlant().getId());
        }
    }

    @Override
    public void readTileNBT(@Nonnull NBTTagCompound tag) {
        Preconditions.checkNotNull(tag);
        final IAgriStat stat = AgriApi.getStatRegistry().valueOf(tag).orElse(null);
        final IAgriPlant plant = AgriApi.getPlantRegistry().get(tag.getString(AgriNBT.SEED)).orElse(null);
        if (stat != null && plant != null) {
            this.seed = new AgriSeed(plant, stat);
        } else {
            this.seed = null;
        }
        this.growthStage = tag.getInteger(AgriNBT.META);
        this.crossCrop = tag.getBoolean(AgriNBT.CROSS_CROP);
        this.destabilizing = tag.getBoolean(AgriNBT.DESTABILIZING);
        this.destabilizeExpiry = tag.getLong(AgriNBT.DESTABILIZE_EXPIRY);
        this.autoFertilize = tag.getBoolean(AgriNBT.AUTO_FERTILIZE);
        this.autoWater = tag.getBoolean(AgriNBT.AUTO_WATER);
    }

    /**
     * Apply a GROWTH increment
     */
    public void applyGrowthTick() {
        // If remote, abort.
        if (this.isRemote()) {
            return;
        }

        // If have no plant, abort.
        if (!this.hasSeed()) {
            return;
        }

        // If mature, abort.
        if (this.isMature()) {
            return;
        }

        // If not fertile, abort.
        if (!this.isFertile()) {
            return;
        }

        // Apply the growth tick.
        setGrowthStage(this.growthStage + 1);

        // The operation was a success.
        // return;
    }

    /**
     * the code that makes the crop cross with neighboring crops
     */
    public void crossOver() {
        AgriApi.getMutationEngine().attemptCross(this, this.getWorld().rand);
    }

    @Override
    public void addServerDebugInfo(@Nonnull Consumer<String> consumer) {
        Preconditions.checkNotNull(consumer);
        consumer.accept("CROP:");
        if (this.crossCrop) {
            consumer.accept(" - This is a crosscrop");
        } else if (this.hasSeed()) {
            final IAgriPlant plant = this.getSeed().getPlant();
            final IAgriStat stat = this.getSeed().getStat();
            if (plant.isWeed()) {
                consumer.accept(" - This crop has weeds");
            } else {
                consumer.accept(" - This crop has a plant");
            }
            consumer.accept(" - Plant: " + plant.getPlantName());
            consumer.accept(" - Id: " + plant.getId());
            consumer.accept(" - Stage: " + this.getGrowthStage());
            consumer.accept(" - Stages: " + plant.getGrowthStages());
            consumer.accept(" - Meta: " + this.getGrowthStage());
            for (CropStat cropStat : CropStat.values()) {
                consumer.accept(" - " + cropStat.getId() + ": " + stat.get(cropStat));
            }
            consumer.accept(" - Fertile: " + this.isFertile());
            consumer.accept(" - Mature: " + this.isMature());
            consumer.accept(" - Destabilizing: " + this.destabilizing);
            consumer.accept(" - AgriSoil: " + plant.getGrowthRequirement().getSoilCompositions().stream()
                    .findFirst().map(ISoilComposition::getId).orElse("None")
            );
        } else {
            consumer.accept(" - This crop has no plant");
        }
    }

    @Override
    public void addClientDebugInfo(@Nonnull Consumer<String> consumer) {
        Preconditions.checkNotNull(consumer);
        if (this.hasSeed()) {
            consumer.accept(" - Texture: " + this.getSeed().getPlant().getPrimaryPlantTexture(this.getGrowthStage()).toString());
        }
    }

    @Override
    public void addDisplayInfo(@Nonnull Consumer<String> information) {
        // Validate
        Preconditions.checkNotNull(information);

        // Add Soil Information
        information.accept(AgriCore.getTranslator().translate("agricrops_tooltip.soil") + ": " + this.getSoilProfile().map(p -> p.getComposition().getName()).orElse("Unknown"));

        if (this.hasSeed()) {
            // Fetch the plant.
            final IAgriPlant plant = this.getSeed().getPlant();
            // Fetch the stat.
            final IAgriStat stat = this.getSeed().getStat();

            //Add the SEED name.
            information.accept(AgriCore.getTranslator().translate("agricrops_tooltip.seed") + ": " + plant.getSeedName());
            //Add the GROWTH.
            if (this.isMature()) {
                information.accept(AgriCore.getTranslator().translate("agricrops_tooltip.growth") + ": " + AgriCore.getTranslator().translate("agricrops_tooltip.mature"));
            } else {
                information.accept(AgriCore.getTranslator().translate("agricrops_tooltip.growth") + ": " + (int) (100.0 * (this.getGrowthStage() + 1) / plant.getGrowthStages()) + "%");
            }
            //Add the ANALYZED data.
            if (stat.isAnalyzed()) {
                stat.addStats(information);
            } else {
                information.accept(AgriCore.getTranslator().translate("agricrops_tooltip.analyzed"));
            }
            //Add the fertility information.
            information.accept(AgriCore.getTranslator().translate(this.isFertile() ? "agricrops_tooltip.fertile" : "agricrops_tooltip.notFertile"));
        } else {
            information.accept(AgriCore.getTranslator().translate("agricrops_tooltip.empty"));
        }

    }

    // =========================================================================
    // OpenComputers - the crop_stick component (crop-mod-design.md §9, §11 step 8)
    // <editor-fold>
    // =========================================================================
    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.isRemote() && Loader.isModLoaded("opencomputers")) {
            this.ocConnect();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (Loader.isModLoaded("opencomputers")) {
            this.ocDisconnect();
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (Loader.isModLoaded("opencomputers")) {
            this.ocDisconnect();
        }
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    private void ocConnect() {
        if (this.ocNode == null) {
            this.ocNode = Network.newNode(this, Visibility.Network).withComponent("crop_stick").create();
        }
        Network.joinOrCreateNetwork(this);
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    private void ocDisconnect() {
        if (this.ocNode != null) {
            ((Node) this.ocNode).remove();
            this.ocNode = null;
        }
    }

    /**
     * Pushes an OC signal event to any computer reachable from this crop stick's node, if
     * OpenComputers is installed and this crop stick is actually connected to a network yet
     * (e.g. not before the first {@link #onLoad()}). No-op otherwise - signal events are
     * a telemetry nicety, never load-bearing for the crop's own logic.
     */
    private void pushSignal(@Nonnull String name, Object... args) {
        if (!this.isRemote() && Loader.isModLoaded("opencomputers")) {
            this.pushSignalOC(name, args);
        }
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    private void pushSignalOC(String name, Object[] args) {
        final Node n = (Node) this.ocNode;
        if (n != null) {
            final Object[] payload = new Object[args.length + 1];
            payload[0] = name;
            System.arraycopy(args, 0, payload, 1, args.length);
            n.sendToReachable("computer.signal", payload);
        }
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public Node node() {
        return (Node) this.ocNode;
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public void onConnect(Node node) {
        // Nothing to do - no other-node-driven state to react to yet.
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public void onDisconnect(Node node) {
        // Nothing to do - see onConnect.
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public void onMessage(Message message) {
        // No inbound network messages are handled directly - all interaction goes through
        // invoke() (pull) or pushSignal (push).
    }

    @Override
    @Nonnull
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "crop_stick";
    }

    @Override
    @Nonnull
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public String[] methods() {
        final List<String> ids = new ArrayList<>();
        ids.add("setAutoFertilize");
        ids.add("setAutoWater");
        ids.add("isAutoFertilize");
        ids.add("isAutoWater");
        for (IAgriPeripheralMethod method : AgriApi.getPeripheralMethodRegistry().all()) {
            if (method.appliesToCrop()) {
                ids.add(method.getId());
            }
        }
        return ids.toArray(new String[0]);
    }

    @Override
    @Nullable
    @net.minecraftforge.fml.common.Optional.Method(modid = "opencomputers")
    public Object[] invoke(@Nullable String methodName, Context context, @Nullable Arguments args) throws Exception {
        // The auto-fertilize/auto-water gates are crop_stick-exclusive (they don't make
        // sense proxied through the analyzer's directional lookup), so they're handled
        // directly here instead of going through the shared IAgriPeripheralMethod registry.
        if ("setAutoFertilize".equals(methodName) || "setAutoWater".equals(methodName)) {
            final boolean value = args != null && args.checkBoolean(0);
            if ("setAutoFertilize".equals(methodName)) {
                this.setAutoFertilize(value);
            } else {
                this.setAutoWater(value);
            }
            return new Object[]{value};
        } else if ("isAutoFertilize".equals(methodName)) {
            return new Object[]{this.isAutoFertilize()};
        } else if ("isAutoWater".equals(methodName)) {
            return new Object[]{this.isAutoWater()};
        }

        final IAgriPeripheralMethod method = AgriApi.getPeripheralMethodRegistry().get(methodName).orElse(null);

        if (method == null || !method.appliesToCrop()) {
            return null;
        }

        final Object[] argObjects = (args == null) ? null : args.toArray();

        try {
            return method.call(this.getWorld(), this.getPos(), null, argObjects);
        } catch (IAgriPeripheralMethod.InvocationException e) {
            throw new Exception(e.getDescription());
        }
    }
    // --------------------------------------------------
    // </editor-fold>
    // =========================================================================
}
