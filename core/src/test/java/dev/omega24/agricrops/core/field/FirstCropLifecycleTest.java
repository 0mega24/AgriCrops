package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropCatalog;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.genetics.StandardStat;
import dev.omega24.agricrops.core.growth.GrowthPhase;
import dev.omega24.agricrops.core.growth.GrowthTimeline;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class FirstCropLifecycleTest {
    private CropDefinition crop(String id, String seed) {
        return new CropDefinition(id, seed, "minecraft:wheat", "minecraft:farmland", "agricrops.crop.wheat",
                new GrowthTimeline(Arrays.asList(new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, 40),
                        new GrowthTimeline.Segment(GrowthPhase.FLOWERING, 40))), StandardGenome.neutral(), 1, 1, 9, 15);
    }
    @Test public void developmentIsLimitedToActuallyPaidResources() {
        CropDefinition def = crop("agricrops:wheat", "minecraft:wheat_seeds");
        CropActions.Step first = CropActions.step(def, CropState.fresh(def, SeedOrigin.WILD),
                new SoilCell(def.soil(), 5, 100), 15);
        assertEquals(5, first.crop.progress()); assertEquals(0, first.soil.water()); assertEquals(95, first.soil.nutrients());
        CropActions.Step dry = CropActions.step(def, first.crop, first.soil, 15);
        assertEquals(5, dry.crop.progress()); assertEquals(CropActions.Reason.WATER, dry.reason);
        assertEquals(95, dry.soil.nutrients());
    }
    @Test public void completeLifecyclePaysCostsAndCannotRepeatFreeHarvest() {
        CropDefinition def = crop("agricrops:wheat", "minecraft:wheat_seeds");
        CropState state = CropState.fresh(def, SeedOrigin.CRAFTED);
        SoilCell soil = new SoilCell(def.soil(), 100, 100);
        for (int i = 0; i < 4; i++) { CropActions.Step step = CropActions.step(def, state, soil, 15); state = step.crop; soil = step.soil; }
        assertEquals(20, soil.water()); assertEquals(20, soil.nutrients());
        CropActions.Harvest first = CropActions.harvest(def, state);
        assertEquals(1, first.count); assertEquals(0, first.crop.progress());
        assertEquals(SeedOrigin.CRAFTED, first.crop.origin());
        assertEquals(0, CropActions.harvest(def, first.crop).count);
    }
    @Test public void yieldCannotIncreaseOutputWithoutItsProductionCost() {
        CropDefinition def = crop("agricrops:wheat", "minecraft:wheat_seeds");
        CropState state = new CropState(def.id(), StandardGenome.neutral().with(StandardStat.YIELD, 16), SeedOrigin.BRED, 0);
        SoilCell soil = new SoilCell(def.soil(), 160, 160);
        for (int i = 0; i < 4; i++) { CropActions.Step step = CropActions.step(def, state, soil, 15); state = step.crop; soil = step.soil; }
        assertEquals(0, soil.nutrients()); assertEquals(0, soil.water()); assertEquals(2, CropActions.harvest(def, state).count);
    }
    @Test public void blockedConditionsConsumeNeitherProgressNorResources() {
        CropDefinition def = crop("agricrops:wheat", "minecraft:wheat_seeds");
        CropState state = CropState.fresh(def, SeedOrigin.WILD); SoilCell soil = new SoilCell(def.soil(), 100, 100);
        CropActions.Step dark = CropActions.step(def, state, soil, 8);
        assertEquals(CropActions.Reason.LIGHT, dark.reason); assertSame(state, dark.crop); assertSame(soil, dark.soil);
        CropActions.Step wrong = CropActions.step(def, state, soil.reconcile("minecraft:sand"), 15);
        assertEquals(CropActions.Reason.SOIL, wrong.reason); assertEquals(0, wrong.crop.progress());
    }
    @Test public void observedSoilReplacementDoesNotCreateNewFertility() {
        SoilCell soil = new SoilCell("minecraft:farmland", 30, 20);
        assertSame(soil, soil.reconcile("minecraft:farmland"));
        SoilCell replaced = soil.reconcile("minecraft:dirt").reconcile("minecraft:farmland");
        assertEquals(0, replaced.nutrients()); assertEquals(0, replaced.water());
    }
    @Test public void ambiguousSeedDefinitionsDoNotFreezeAPartialCatalog() {
        CropCatalog catalog = new CropCatalog();
        catalog.put(crop("agricrops:wheat", "minecraft:wheat_seeds"), false);
        catalog.put(crop("pack:other", "minecraft:wheat_seeds"), false);
        try { catalog.freeze(); fail("Ambiguous native seed should fail"); } catch (IllegalArgumentException expected) {}
        catalog.put(crop("pack:other", "minecraft:beetroot_seeds"), true);
        assertEquals(2, catalog.freeze().size());
        try { catalog.put(crop("pack:late", "minecraft:carrot"), false); fail("Late edit"); } catch (IllegalStateException expected) {}
    }
}
