/*
 */
package dev.omega24.agricrops.impl.v1;

import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import javax.annotation.Nonnull;

/**
 * Immutable {@link ISoilProfile} implementation.
 */
public class SoilProfile implements ISoilProfile {

    @Nonnull
    private final ISoilComposition composition;
    private final int nutrientCapacity;
    private final float moistureRetention;
    private final float growthMultiplier;

    public SoilProfile(@Nonnull ISoilComposition composition, int nutrientCapacity,
                    float moistureRetention, float growthMultiplier) {
        this.composition = composition;
        this.nutrientCapacity = nutrientCapacity;
        this.moistureRetention = moistureRetention;
        this.growthMultiplier = growthMultiplier;
    }

    @Nonnull
    @Override
    public ISoilComposition getComposition() {
        return this.composition;
    }

    @Override
    public int getNutrientCapacity() {
        return this.nutrientCapacity;
    }

    @Override
    public float getMoistureRetention() {
        return this.moistureRetention;
    }

    @Override
    public float getGrowthMultiplier() {
        return this.growthMultiplier;
    }

}
