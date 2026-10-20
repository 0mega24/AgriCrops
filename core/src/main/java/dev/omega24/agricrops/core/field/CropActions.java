package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.genetics.StandardStat;
import dev.omega24.agricrops.core.genetics.TraitDefinition;
import dev.omega24.agricrops.core.environment.Environment;
import dev.omega24.agricrops.core.environment.Phenotype;
import java.util.*;

/** Authoritative first-slice actions. No resources means no unpaid development. */
public final class CropActions {
    public static final int UPDATE_TICKS = 20;
    public enum Reason { GROWING, MATURE, SOIL, LIGHT, WATER, NUTRIENTS, ENVIRONMENT, FACTOR_MISSING, FOUNDATION, ROOTING, PRODUCTION }

    public static final class Step {
        public final CropState crop;
        public final SoilCell soil;
        public final Reason reason;
        public final RootZone roots;
        public final Phenotype phenotype;
        public final String detail;
        private Step(CropState crop, RootZone roots, Reason reason, Phenotype phenotype, String detail) {
            this.crop = crop; this.soil = roots.cells.get(0); this.roots = roots; this.reason = reason; this.phenotype = phenotype; this.detail = detail;
        }
    }

    public static final class Harvest {
        public final CropState crop;
        public final int count;
        public final Map<String, Integer> products;
        private Harvest(CropState crop, Map<String, Integer> products) { this.crop = crop; this.products = products; this.count = products.values().stream().mapToInt(Integer::intValue).sum(); }
    }

    private CropActions() {}

    public static Step step(CropDefinition definition, CropState crop, SoilCell soil, int light) {
        return step(definition, crop, RootZone.single(soil), Environment.reference(light), Collections.singletonMap(definition.id(), definition), Collections.emptyMap());
    }

    public static Step step(CropDefinition definition, CropState crop, RootZone roots, Environment environment,
                            Map<String, CropDefinition> crops, Map<String, TraitDefinition> traits) {
        requireSameSpecies(definition, crop);
        Phenotype phenotype = Phenotype.evaluate(definition, crop, roots, environment, traits);
        Set<String> sources = new TreeSet<>(crop.specimen().products.weights().keySet()); sources.add(definition.id());
        for (String id : sources) {
            CropDefinition source = crops.get(id);
            if (source == null) throw new IllegalArgumentException("Missing active component " + id);
            if (!roots.cells.get(0).block().equals(source.soil())) return new Step(crop, roots, Reason.SOIL, phenotype, source.soil());
            for (Map.Entry<Integer, String> foundation : source.rules().foundations.entrySet()) {
                if (foundation.getKey() > roots.cells.size() || !roots.cells.get(foundation.getKey() - 1).block().equals(foundation.getValue())) return new Step(crop, roots, Reason.FOUNDATION, phenotype, foundation.getKey() + ":" + foundation.getValue());
            }
            if (!id.equals(definition.id())) {
                Phenotype gates = Phenotype.sourceGates(source, crop, roots, environment, traits, phenotype.reach);
                if (gates.hardBlocked) return new Step(crop, roots, gates.missing ? Reason.FACTOR_MISSING
                        : gates.limiting.equals("agricrops:light") ? Reason.LIGHT : Reason.ENVIRONMENT, gates, id + " / " + gates.limiting);
            }
        }
        if (phenotype.hardBlocked) return new Step(crop, roots, phenotype.missing ? Reason.FACTOR_MISSING : phenotype.limiting.equals("agricrops:light") ? Reason.LIGHT : Reason.ENVIRONMENT, phenotype, phenotype.limiting);
        if (phenotype.rate <= 0) return new Step(crop, roots, Reason.ENVIRONMENT, phenotype, phenotype.limiting);
        int availableWater = roots.availableWater(phenotype.reach), availableNutrients = roots.availableNutrients(phenotype.reach);
        if (crop.rooting() > 0) {
            int work = (int) Math.min(20, Math.min(crop.rooting(), Math.min(availableWater, availableNutrients)));
            if (work == 0) return new Step(crop, roots, availableWater == 0 ? Reason.WATER : Reason.NUTRIENTS, phenotype, "rooting");
            return new Step(crop.advance(crop.progress(), crop.rooting() - work, 0, 0), roots.consume(work, work, phenotype.reach), Reason.ROOTING, phenotype, "rooting");
        }
        CropState initial = crop;
        if (crop.plan() == null) crop = crop.planned(ProductionPlan.resolve(definition, crop, crops, phenotype.waterMultiplier, phenotype.nutrientMultiplier));
        if (crop.progress() >= definition.timeline().totalTicks()) {
            if (crop.funded()) return new Step(crop, roots, Reason.MATURE, phenotype, "");
            // Conservative schema-1 migration: keep progress/genes, but never invent paid reserves.
            int w = Math.min(availableWater, Math.min(100, crop.plan().water - crop.paidWater()));
            int n = Math.min(availableNutrients, Math.min(100, crop.plan().nutrients - crop.paidNutrients()));
            CropState next = crop.advance(crop.progress(), 0, crop.paidWater() + w, crop.paidNutrients() + n);
            return new Step(next, roots.consume(w, n, phenotype.reach), next.funded() ? Reason.MATURE : Reason.PRODUCTION, phenotype, "migration funding");
        }
        long total = definition.timeline().totalTicks(), span = total - crop.cycleStart();
        int requested = (int) Math.min(Math.max(1, (int) (UPDATE_TICKS * phenotype.rate)), total - crop.progress());
        int work = requested, water = 0, nutrients = 0;
        while (work > 0) {
            long elapsed = crop.progress() + work - crop.cycleStart();
            water = Math.max(0, (int) ((crop.plan().water * elapsed + span - 1) / span) - crop.paidWater());
            nutrients = Math.max(0, (int) ((crop.plan().nutrients * elapsed + span - 1) / span) - crop.paidNutrients());
            if (water <= availableWater && nutrients <= availableNutrients) break;
            work--;
        }
        if (work == 0) return new Step(initial, roots, water > availableWater ? Reason.WATER : Reason.NUTRIENTS, phenotype, "stock or uptake");
        CropState next = crop.advance(crop.progress() + work, 0, crop.paidWater() + water, crop.paidNutrients() + nutrients);
        return new Step(next, roots.consume(water, nutrients, phenotype.reach), next.progress() == total && next.funded() ? Reason.MATURE : Reason.GROWING, phenotype, phenotype.limiting);
    }

    public static Harvest harvest(CropDefinition definition, CropState crop) {
        requireSameSpecies(definition, crop);
        return crop.rooting() == 0 && crop.progress() >= definition.timeline().totalTicks() && crop.funded()
                ? new Harvest(crop.harvested(definition.regrowthTicks()), crop.plan().products) : new Harvest(crop, Collections.emptyMap());
    }

    private static void requireSameSpecies(CropDefinition definition, CropState crop) {
        if (!definition.id().equals(crop.crop())) throw new IllegalArgumentException("Definition/specimen mismatch");
    }
}
