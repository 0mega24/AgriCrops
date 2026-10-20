package dev.omega24.agricrops.core.environment;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.field.*;
import dev.omega24.agricrops.core.genetics.*;
import dev.omega24.agricrops.core.growth.GrowthPhase;
import java.util.*;

/** One bounded limiting fit, not a multiplication of many unexplained biome bonuses. */
public final class Phenotype {
    public final double rate, fit, waterMultiplier, nutrientMultiplier;
    public final int reach;
    public final String limiting;
    public final boolean hardBlocked, missing;
    private Phenotype(double rate, double fit, int reach, String limiting, boolean blocked, boolean missing, double water, double nutrients) {
        this.rate = rate; this.fit = fit; this.reach = reach; this.limiting = limiting; hardBlocked = blocked; this.missing = missing;
        waterMultiplier = water; nutrientMultiplier = nutrients;
    }
    public static Phenotype evaluate(CropDefinition definition, CropState state, RootZone roots, Environment raw, Map<String, TraitDefinition> definitions) {
        return evaluate(definition, state, roots, raw, definitions, null);
    }
    /** Source gates use the actual body's accessible roots, not an invented second root inventory. */
    public static Phenotype sourceGates(CropDefinition definition, CropState state, RootZone roots, Environment raw, Map<String, TraitDefinition> definitions, int reach) {
        if (reach < 1 || reach > 3) throw new IllegalArgumentException("Actual root reach must be 1..3");
        return evaluate(definition, state, roots, raw, definitions, reach);
    }
    private static Phenotype evaluate(CropDefinition definition, CropState state, RootZone roots, Environment raw, Map<String, TraitDefinition> definitions, Integer actualReach) {
        Map<String, Double> traits = state.specimen().traits.expressed(definitions);
        GrowthPhase phase = definition.timeline().phaseAt(state.progress());
        int reach = phase == GrowthPhase.GERMINATION ? 1 : phase == GrowthPhase.VEGETATIVE ? 2 : 3;
        reach = Math.min(reach, definition.rules().roots);
        double water = 1, nutrients = 1;
        for (Map.Entry<String, Double> trait : traits.entrySet()) {
            TraitDefinition type = definitions.get(trait.getKey());
            if (type.effect == TraitDefinition.Effect.ROOT_DEPTH) reach += (int) Math.floor(trait.getValue());
            if (type.effect == TraitDefinition.Effect.WATER_DEMAND) water += trait.getValue();
            if (type.effect == TraitDefinition.Effect.NUTRIENT_DEMAND) nutrients += trait.getValue();
        }
        reach = Math.max(1, Math.min(3, reach));
        if (actualReach != null) reach = actualReach;
        water = Math.max(0.5, Math.min(3, water)); nutrients = Math.max(0.5, Math.min(3, nutrients));
        Environment environment = raw.with("agricrops:moisture", roots.moisture(reach), "accessible root cells")
                .with("agricrops:nutrition", roots.nutrition(reach), "accessible root cells")
                .with("agricrops:aeration", roots.aeration(reach), "soil saturation/profile");
        List<Requirement> requirements = new ArrayList<>(definition.rules().requirements);
        if (requirements.stream().noneMatch(rule -> rule.factor.equals("agricrops:light"))) requirements.add(new Requirement("agricrops:light", definition.minLight(), definition.maxLight(), 0, true));
        double fit = 1; String limiting = "agricrops:light";
        for (Requirement requirement : requirements) {
            if (!environment.has(requirement.factor)) return new Phenotype(0, 0, reach, requirement.factor, true, true, water, nutrients);
            double shift = 0, extra = 0;
            if (requirement.factor.equals("agricrops:light")) shift = state.genome().get(StandardStat.LIGHT_TOLERANCE) * 4.0 / 63;
            if (requirement.factor.equals("agricrops:temperature")) shift = state.genome().get(StandardStat.TEMPERATURE_TOLERANCE) * 0.5 / 63;
            for (Map.Entry<String, Double> trait : traits.entrySet()) {
                TraitDefinition type = definitions.get(trait.getKey());
                boolean applies = type.effect == TraitDefinition.Effect.TOLERANCE
                        || (type.effect == TraitDefinition.Effect.COLD_TOLERANCE && environment.get(requirement.factor) < requirement.minimum + shift)
                        || (type.effect == TraitDefinition.Effect.HEAT_TOLERANCE && environment.get(requirement.factor) > requirement.maximum + shift);
                if (applies && type.factor.equals(requirement.factor) && !requirement.hard) extra += trait.getValue();
            }
            double current = requirement.fit(environment.get(requirement.factor), shift, extra);
            if (requirement.hard && requirement.factor.equals("agricrops:light")) current = environment.get(requirement.factor) >= Math.max(0, Math.floor(requirement.minimum + shift))
                    && environment.get(requirement.factor) <= Math.min(15, Math.ceil(requirement.maximum + shift)) ? 1 : 0;
            if (requirement.hard && current < 1) return new Phenotype(0, 0, reach, requirement.factor, true, false, water, nutrients);
            current += (1 - current) * state.genome().get(StandardStat.RESILIENCE) / 126.0;
            if (current < fit) { fit = current; limiting = requirement.factor; }
        }
        double moisture = roots.moisture(reach), nutrition = roots.nutrition(reach);
        double affinity = 1 + (moisture - 0.5) * state.genome().get(StandardStat.WATER_AFFINITY) / 315.0;
        double response = 1 + (nutrition - 0.5) * state.genome().get(StandardStat.FERTILITY_RESPONSE) / 157.5;
        double rate = Math.min(2.5, (1 + state.genome().get(StandardStat.GROWTH) / 63.0) * fit * affinity * response);
        return new Phenotype(Math.max(0, rate), fit, reach, limiting, false, false, water, nutrients);
    }
}
