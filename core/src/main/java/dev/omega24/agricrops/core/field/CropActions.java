package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.genetics.StandardStat;

/** Authoritative first-slice actions. No resources means no unpaid development. */
public final class CropActions {
    public static final int UPDATE_TICKS = 20;
    public enum Reason { GROWING, MATURE, SOIL, LIGHT, WATER, NUTRIENTS }

    public static final class Step {
        public final CropState crop;
        public final SoilCell soil;
        public final Reason reason;
        private Step(CropState crop, SoilCell soil, Reason reason) {
            this.crop = crop; this.soil = soil; this.reason = reason;
        }
    }

    public static final class Harvest {
        public final CropState crop;
        public final int count;
        private Harvest(CropState crop, int count) { this.crop = crop; this.count = count; }
    }

    private CropActions() {}

    public static Step step(CropDefinition definition, CropState crop, SoilCell soil, int light) {
        requireSameSpecies(definition, crop);
        if (!soil.block().equals(definition.soil())) return new Step(crop, soil, Reason.SOIL);
        if (crop.progress() >= definition.timeline().totalTicks()) return new Step(crop, soil, Reason.MATURE);
        if (light < definition.minLight() || light > definition.maxLight()) return new Step(crop, soil, Reason.LIGHT);
        int yieldScale = outputCount(crop);
        int waterCost = definition.waterCost() * yieldScale;
        int nutrientCost = definition.nutrientCost() * yieldScale;
        long requested = UPDATE_TICKS * (63L + crop.genome().get(StandardStat.GROWTH)) / 63;
        long possible = Math.min(requested, definition.timeline().totalTicks() - crop.progress());
        possible = Math.min(possible, Math.min(soil.water() / waterCost, soil.nutrients() / nutrientCost));
        if (possible == 0) return new Step(crop, soil,
                soil.water() < waterCost ? Reason.WATER : Reason.NUTRIENTS);
        SoilCell paid = soil.consume((int) possible * waterCost, (int) possible * nutrientCost);
        CropState next = crop.at(crop.progress() + possible);
        return new Step(next, paid, next.progress() == definition.timeline().totalTicks() ? Reason.MATURE : Reason.GROWING);
    }

    public static Harvest harvest(CropDefinition definition, CropState crop) {
        requireSameSpecies(definition, crop);
        return crop.progress() >= definition.timeline().totalTicks()
                ? new Harvest(crop.at(0), outputCount(crop)) : new Harvest(crop, 0);
    }

    private static int outputCount(CropState crop) { return 1 + crop.genome().get(StandardStat.YIELD) / 16; }
    private static void requireSameSpecies(CropDefinition definition, CropState crop) {
        if (!definition.id().equals(crop.crop())) throw new IllegalArgumentException("Definition/specimen mismatch");
    }
}
