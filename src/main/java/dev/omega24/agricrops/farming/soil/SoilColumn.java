/*
 */
package dev.omega24.agricrops.farming.soil;

import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import javax.annotation.Nonnull;

/**
 * Mutable {@link ISoilColumn} implementation.
 */
public class SoilColumn implements ISoilColumn {

    public static final int FULL_BLOCK_DEPTH = 1000;

    /**
     * Ceiling for the moisture value, per crop-mod-design.md §4: "extend vanilla farmland's
     * 0-7 moisture byte to a wider int (e.g. 0-100)".
     */
    public static final int MAX_MOISTURE = 100;

    private int depth;
    @Nonnull
    private ISoilComposition composition;
    private int nutrientCapacity;
    private int nutrients;
    private int moisture;

    public SoilColumn(@Nonnull ISoilProfile profile) {
        this.depth = FULL_BLOCK_DEPTH;
        this.composition = profile.getComposition();
        this.nutrientCapacity = profile.getNutrientCapacity();
        // Fresh soil starts fully fertile; growth ticks drain it from here, fertilizer refills it.
        this.nutrients = this.nutrientCapacity;
        // Fresh soil starts dry; a nearby water source (or irrigation) has to earn the moisture.
        this.moisture = 0;
    }

    public SoilColumn(int depth, @Nonnull ISoilComposition composition, int nutrientCapacity, int nutrients, int moisture) {
        this.depth = depth;
        this.composition = composition;
        this.nutrientCapacity = nutrientCapacity;
        this.nutrients = nutrients;
        this.moisture = moisture;
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public void setDepth(int milliLayers) {
        this.depth = milliLayers;
    }

    @Nonnull
    @Override
    public ISoilComposition getComposition() {
        return this.composition;
    }

    @Override
    public void setComposition(@Nonnull ISoilComposition composition) {
        this.composition = composition;
    }

    @Override
    public int getNutrients() {
        return this.nutrients;
    }

    @Override
    public int getNutrientCapacity() {
        return this.nutrientCapacity;
    }

    @Override
    public void addNutrients(int amount) {
        this.nutrients = Math.min(this.nutrientCapacity, this.nutrients + amount);
    }

    @Override
    public int drainNutrients(int amount) {
        final int drained = Math.min(this.nutrients, amount);
        this.nutrients -= drained;
        return drained;
    }

    @Override
    public int getMoisture() {
        return this.moisture;
    }

    @Override
    public void addMoisture(int amount) {
        this.moisture = Math.min(MAX_MOISTURE, this.moisture + amount);
    }

    @Override
    public void decayMoisture(float retentionRate, int baseAmount) {
        final float retention = Math.max(0.0f, Math.min(1.0f, retentionRate));
        final int decay = Math.round(baseAmount * (1.0f - retention));
        this.moisture = Math.max(0, this.moisture - decay);
    }

}
