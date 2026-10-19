package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.genetics.StandardStat;
import dev.omega24.agricrops.core.growth.GrowthPhase;
import dev.omega24.agricrops.core.growth.GrowthTimeline;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class HarvestRegrowthTest {
    private GrowthTimeline timeline() {
        return new GrowthTimeline(Arrays.asList(
                new GrowthTimeline.Segment(GrowthPhase.GERMINATION, 20),
                new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, 40),
                new GrowthTimeline.Segment(GrowthPhase.FLOWERING, 60),
                new GrowthTimeline.Segment(GrowthPhase.FRUITING, 80)));
    }
    private CropDefinition crop(int target) {
        return new CropDefinition("pack:berry", "pack:berry_seed", "pack:berry", "minecraft:farmland",
                "pack.crop.berry", timeline(), StandardGenome.neutral(), 1, 1, 9, 15, target);
    }
    private CropState mature(CropDefinition crop) {
        return new CropState(crop.id(), StandardGenome.neutral().with(StandardStat.RESILIENCE, 31),
                SeedOrigin.BRED, crop.timeline().totalTicks());
    }
    @Test public void omittedRegrowthUsesNumericStageOne() {
        GrowthTimeline custom = new GrowthTimeline(Arrays.asList(new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, 40)));
        CropDefinition crop = new CropDefinition("pack:leaf", "pack:seed", "pack:leaf", "minecraft:farmland",
                "pack.crop.leaf", custom, StandardGenome.neutral(), 1, 1, 9, 15);
        assertEquals(1, crop.regrowthStage());
        CropState harvested = CropActions.harvest(crop, mature(crop)).crop;
        assertEquals(6, harvested.progress()); assertEquals(1, crop.stageAt(harvested.progress()));
    }
    @Test public void everyRegrowthTargetPreservesTheSpecimenAndPreventsRepeatHarvest() {
        long[] starts = {0, 29, 58, 86, 115, 143, 172};
        for (int i = 0; i < starts.length; i++) {
            CropDefinition crop = crop(i); CropState parent = mature(crop);
            CropActions.Harvest harvest = CropActions.harvest(crop, parent);
            assertEquals(1, harvest.count); assertEquals(starts[i], harvest.crop.progress());
            assertEquals(i, crop.stageAt(harvest.crop.progress()));
            assertSame(parent.genome(), harvest.crop.genome()); assertEquals(parent.origin(), harvest.crop.origin());
            assertEquals(parent.crop(), harvest.crop.crop());
            assertEquals(0, CropActions.harvest(crop, harvest.crop).count);
        }
    }
    @Test public void intermediateRegrowthStillRequiresPaidRemainingDevelopment() {
        CropDefinition crop = crop(5);
        CropState state = CropActions.harvest(crop, mature(crop)).crop;
        CropActions.Step dry = CropActions.step(crop, state, new SoilCell(crop.soil(), 0, 57), 15);
        assertEquals(CropActions.Reason.WATER, dry.reason); assertSame(state, dry.crop);
        assertEquals(0, CropActions.harvest(crop, dry.crop).count);
        SoilCell soil = new SoilCell(crop.soil(), 57, 57);
        for (int i = 0; i < 4; i++) {
            CropActions.Step step = CropActions.step(crop, state, soil, 15); state = step.crop; soil = step.soil;
        }
        assertEquals(0, soil.water()); assertEquals(0, soil.nutrients());
        CropActions.Harvest second = CropActions.harvest(crop, state);
        assertEquals(1, second.count); assertEquals(143, second.crop.progress());
        assertEquals(0, CropActions.harvest(crop, second.crop).count);
    }
    @Test public void prematureHarvestCannotMoveASeedForwardToItsRegrowthTarget() {
        CropDefinition crop = crop(5);
        CropState seedling = CropState.fresh(crop, SeedOrigin.WILD);
        CropActions.Harvest rejected = CropActions.harvest(crop, seedling);
        assertEquals(0, rejected.count); assertSame(seedling, rejected.crop);
    }
    @Test public void matureOrOutOfRangeStageCannotBeARegrowthTarget() {
        for (int stage : new int[]{-1, 7, 8, Integer.MAX_VALUE}) {
            try { crop(stage); fail("Invalid regrowth stage accepted: " + stage); }
            catch (IllegalArgumentException expected) { /* No instant repeat-harvest stage. */ }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unrepresentableStageCannotSilentlySelectADifferentStoredAge() {
        GrowthTimeline leaf = new GrowthTimeline(Arrays.asList(new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, 4)));
        new CropDefinition("pack:leaf", "pack:seed", "pack:leaf", "minecraft:farmland", "pack.crop.leaf",
                leaf, StandardGenome.neutral(), 1, 1, 9, 15, 2);
    }
    @Test public void numericStageBoundariesAreExactAndMaturityIsTerminal() {
        CropDefinition crop = crop(4);
        assertEquals(3, crop.stageAt(114)); assertEquals(4, crop.stageAt(115));
        assertEquals(6, crop.stageAt(199)); assertEquals(7, crop.stageAt(200));
        assertEquals(7, crop.stageAt(Long.MAX_VALUE));
    }
}
