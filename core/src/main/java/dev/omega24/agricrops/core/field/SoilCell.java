package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropDefinition;

/** Position-owned resource state. Breaking a crop does not create another soil inventory. */
public final class SoilCell {
    public static final int CAPACITY = 10000;
    private final String block;
    private final int water, nutrients;

    public SoilCell(String block, int water, int nutrients) {
        this.block = CropDefinition.identifier(block);
        if (water < 0 || water > CAPACITY || nutrients < 0 || nutrients > CAPACITY) {
            throw new IllegalArgumentException("Soil resources must be in 0.." + CAPACITY);
        }
        this.water = water;
        this.nutrients = nutrients;
    }

    public String block() { return block; }
    public int water() { return water; }
    public int nutrients() { return nutrients; }

    public SoilCell add(int waterInput, int nutrientInput) {
        if (waterInput < 0 || nutrientInput < 0) throw new IllegalArgumentException("Negative input");
        return new SoilCell(block, (int) Math.min(CAPACITY, (long) water + waterInput),
                (int) Math.min(CAPACITY, (long) nutrients + nutrientInput));
    }

    public SoilCell consume(int waterAmount, int nutrientAmount) {
        if (waterAmount < 0 || nutrientAmount < 0 || waterAmount > water || nutrientAmount > nutrients) {
            throw new IllegalArgumentException("Cannot overdraw soil");
        }
        return new SoilCell(block, water - waterAmount, nutrients - nutrientAmount);
    }

    public SoilCell reconcile(String actualBlock) {
        return block.equals(actualBlock) ? this : new SoilCell(actualBlock, 0, 0);
    }
}
