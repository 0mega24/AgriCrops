package dev.omega24.agricrops.core.genetics;

import dev.omega24.agricrops.core.content.CropDefinition;

public final class TraitDefinition {
    public enum Effect { TOLERANCE, COLD_TOLERANCE, HEAT_TOLERANCE, ROOT_DEPTH, WATER_DEMAND, NUTRIENT_DEMAND }
    public final String id, locus, factor, nameKey;
    public final Effect effect;
    public final double strength;
    public final int dominance, generations;
    public TraitDefinition(String id, String locus, Effect effect, String factor, double strength, int dominance, int generations, String nameKey) {
        this.id = CropDefinition.identifier(id); this.locus = CropDefinition.identifier(locus);
        this.factor = CropDefinition.identifier(factor); this.effect = java.util.Objects.requireNonNull(effect);
        if (!Double.isFinite(strength) || strength < -0.5 || strength > 2 || dominance < 0 || dominance > 10 || generations < 1 || generations > 10
                || nameKey == null || !nameKey.matches("[a-z0-9_.:-]+")) throw new IllegalArgumentException("Invalid trait " + id);
        this.strength = strength; this.dominance = dominance; this.generations = generations; this.nameKey = nameKey;
    }
}
