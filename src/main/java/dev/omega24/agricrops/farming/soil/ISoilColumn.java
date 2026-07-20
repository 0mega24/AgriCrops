/*
 */
package dev.omega24.agricrops.farming.soil;

import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import javax.annotation.Nonnull;

/**
 * Per-column soil state (crop-mod-design.md §3): depth, composition, and a nutrient
 * pool. Lives on the block below a crop stick, shared by any number of crop sticks in
 * the same column (see {@link IChunkSoilColumns}), rather than duplicated per
 * TileEntity.
 */
public interface ISoilColumn {

    /**
     * Depth in milli-layers; 1000 = one full vanilla block of depth.
     */
    int getDepth();

    void setDepth(int milliLayers);

    @Nonnull
    ISoilComposition getComposition();

    void setComposition(@Nonnull ISoilComposition composition);

    /**
     * The current size of the nutrient pool.
     */
    int getNutrients();

    /**
     * The maximum size of the nutrient pool, from the {@code ISoilProfile} this column
     * was created from.
     */
    int getNutrientCapacity();

    /**
     * Adds to the nutrient pool, clamped to capacity. Used by fertilizer.
     */
    void addNutrients(int amount);

    /**
     * Drains from the nutrient pool, floored at zero. Used by growth ticks.
     *
     * @return the amount actually drained (may be less than requested if the pool ran dry).
     */
    int drainNutrients(int amount);

    /**
     * The current moisture level, 0..{@link SoilColumn#MAX_MOISTURE}.
     */
    int getMoisture();

    /**
     * Adds to the moisture level, clamped to {@link SoilColumn#MAX_MOISTURE}. Used when a
     * water source is found nearby.
     */
    void addMoisture(int amount);

    /**
     * Drains moisture, floored at zero, per crop-mod-design.md §4's "decaying by a
     * configurable rate per tick/random-tick". {@code retentionRate} (from this column's
     * {@code ISoilProfile}, 0..1) softens the drain: 1.0 retains fully (no decay), 0.0
     * retains nothing (full decay every call).
     */
    void decayMoisture(float retentionRate, int baseAmount);

}
