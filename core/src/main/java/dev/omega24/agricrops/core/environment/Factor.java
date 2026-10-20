package dev.omega24.agricrops.core.environment;

import dev.omega24.agricrops.core.content.CropDefinition;

public final class Factor {
    public final String id, unit;
    public final double minimum, maximum;
    public final Double constant;
    public Factor(String id, double minimum, double maximum, String unit, Double constant) {
        this.id = CropDefinition.identifier(id);
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum > maximum || Math.abs(minimum) > 1000000 || Math.abs(maximum) > 1000000
                || unit == null || unit.length() > 64 || (constant != null && (!Double.isFinite(constant) || constant < minimum || constant > maximum))) throw new IllegalArgumentException("Invalid factor definition " + id);
        this.minimum = minimum; this.maximum = maximum; this.unit = unit; this.constant = constant;
    }
    public void validate(double value) { if (!Double.isFinite(value) || value < minimum || value > maximum) throw new IllegalArgumentException(id + " outside declared range"); }
}
