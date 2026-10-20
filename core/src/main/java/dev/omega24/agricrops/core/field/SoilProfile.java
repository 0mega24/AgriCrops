package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropDefinition;

public final class SoilProfile {
    public final String block;
    public final int waterCapacity, nutrientCapacity, evaporation, drainage, release, uptake;
    public final double saturationStart;
    public SoilProfile(String block, int waterCapacity, int nutrientCapacity, int evaporation, int drainage, int release, int uptake, double saturationStart) {
        this.block = CropDefinition.identifier(block);
        if (waterCapacity < 1 || waterCapacity > 10000 || nutrientCapacity < 1 || nutrientCapacity > 10000 || evaporation < 0 || evaporation > 100
                || drainage < 0 || drainage > 1000 || release < 0 || release > 100 || uptake < 1 || uptake > 10000
                || !Double.isFinite(saturationStart) || saturationStart < 0.5 || saturationStart >= 1) throw new IllegalArgumentException("Invalid soil profile");
        this.waterCapacity = waterCapacity; this.nutrientCapacity = nutrientCapacity; this.evaporation = evaporation;
        this.drainage = drainage; this.release = release; this.uptake = uptake; this.saturationStart = saturationStart;
    }
    public SoilCell tick(SoilCell cell, int waterInput, double drying) {
        int water = Math.min(waterCapacity, cell.water() + waterInput);
        water = Math.max(0, water - (int) Math.ceil(evaporation * Math.max(0, Math.min(2, drying))));
        int releaseNow = Math.min(release, Math.min(cell.organic(), nutrientCapacity - Math.min(nutrientCapacity, cell.nutrients())));
        return new SoilCell(block, water, Math.min(nutrientCapacity, cell.nutrients()) + releaseNow, cell.organic() - releaseNow);
    }
    public double aeration(SoilCell cell) {
        double saturation = (double) cell.water() / waterCapacity;
        return saturation <= saturationStart ? 1 : Math.max(0.1, 1 - (saturation - saturationStart) / (1 - saturationStart));
    }
}
