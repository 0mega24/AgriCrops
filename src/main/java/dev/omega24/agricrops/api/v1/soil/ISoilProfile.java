/*
 */
package dev.omega24.agricrops.api.v1.soil;

import javax.annotation.Nonnull;

/**
 * The growth-mechanics profile registered for a block, per crop-mod-design.md §3: a
 * composition tag plus base numbers for that surface. None of this is hardcoded
 * per-block logic — it's registered against {@link ISoilProfileRegistry}, the same
 * open-registry pattern as plants/mutations.
 */
public interface ISoilProfile {

    /**
     * The composition tag for this surface (dirt, tilled, sand, clay, stone, ...).
     */
    @Nonnull
    ISoilComposition getComposition();

    /**
     * The maximum size of the nutrient pool a {@code SoilColumn} on this surface can hold.
     */
    int getNutrientCapacity();

    /**
     * How well this surface retains moisture, 0..1. Scales down the per-tick moisture decay
     * on a {@code SoilColumn} on this surface (crop-mod-design.md §4/§11 step 3): 1.0 retains
     * fully (no decay), 0.0 retains nothing (full decay every tick).
     */
    float getMoistureRetention();

    /**
     * A flat multiplier applied to growth chance on this surface, independent of any
     * plant's soil preference.
     */
    float getGrowthMultiplier();

}
