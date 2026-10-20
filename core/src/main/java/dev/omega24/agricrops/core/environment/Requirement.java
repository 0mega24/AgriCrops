package dev.omega24.agricrops.core.environment;

import dev.omega24.agricrops.core.content.CropDefinition;

public final class Requirement {
    public final String factor;
    public final double minimum, maximum, tolerance;
    public final boolean hard;
    public Requirement(String factor, double minimum, double maximum, double tolerance, boolean hard) {
        this.factor = CropDefinition.identifier(factor);
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || !Double.isFinite(tolerance)
                || minimum > maximum || tolerance < 0 || Math.abs(minimum) > 1000000 || Math.abs(maximum) > 1000000 || tolerance > 1000000) {
            throw new IllegalArgumentException("Invalid environmental band for " + factor);
        }
        this.minimum = minimum; this.maximum = maximum; this.tolerance = tolerance; this.hard = hard;
    }
    public double fit(double value, double shift, double extraTolerance) {
        double distance = Math.max(0, Math.max(minimum + shift - value, value - maximum - shift));
        double width = tolerance + extraTolerance;
        return distance == 0 ? 1 : width == 0 ? 0 : Math.max(0, 1 - distance / width);
    }
}
