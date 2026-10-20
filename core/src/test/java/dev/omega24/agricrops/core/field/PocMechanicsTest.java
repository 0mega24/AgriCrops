package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.*;
import dev.omega24.agricrops.core.environment.*;
import dev.omega24.agricrops.core.genetics.*;
import dev.omega24.agricrops.core.growth.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PocMechanicsTest {
    private CropDefinition crop(String name, PlantRules rules) {
        return new CropDefinition("test:" + name, "test:" + name + "_seed", "test:" + name, "test:soil", "test." + name,
                new GrowthTimeline(Arrays.asList(new GrowthTimeline.Segment(GrowthPhase.GERMINATION, 100), new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, 100),
                        new GrowthTimeline.Segment(GrowthPhase.FLOWERING, 100), new GrowthTimeline.Segment(GrowthPhase.FRUITING, 100))), StandardGenome.neutral(), 1, 1, 9, 15).withRules(rules);
    }
    private PlantRules.Builder rules() { return PlantRules.builder().roots(3).harvestUnits(2); }
    private CropState state(CropDefinition d, StandardGenome genes, TraitGenome traits, long lineage, long progress) {
        return CropState.plant(new Specimen(d.id(), genes, SeedOrigin.BRED, ProductMix.single(d.id()), traits, lineage, 1), 0).at(progress);
    }
    private RootZone roots(int first, int second, int third) {
        SoilProfile p = new SoilProfile("test:soil", 10000, 10000, 0, 0, 10, 1000, .9);
        return new RootZone(Arrays.asList(new SoilCell(p.block, first, first), new SoilCell(p.block, second, second), new SoilCell(p.block, third, third)), Arrays.asList(p, p, p));
    }
    private Phenotype phenotype(CropDefinition d, CropState s, RootZone r, Environment e) { return Phenotype.evaluate(d, s, r, e, Collections.emptyMap()); }
    @Test public void climateChangesExpressionNotTheSavedGenome() {
        CropDefinition d = crop("a", rules().requirement(new Requirement("agricrops:temperature", .5, 1, 1, false)).build());
        CropState s = CropState.fresh(d, SeedOrigin.WILD); RootZone r = roots(5000, 5000, 5000);
        assertEquals(1, phenotype(d, s, r, Environment.reference(15)).fit, 0);
        assertEquals(.5, phenotype(d, s, r, Environment.reference(15).with("agricrops:temperature", 0, "test")).fit, .0001);
        assertEquals(StandardGenome.neutral(), s.genome());
    }
    @Test public void directionalAdaptationHasTradeoffsAndResilienceCannotBypassHardRules() {
        CropDefinition d = crop("a", rules().requirement(new Requirement("agricrops:temperature", .5, 1, 1, false)).build());
        CropState hot = state(d, StandardGenome.neutral().with(StandardStat.TEMPERATURE_TOLERANCE, 63).with(StandardStat.RESILIENCE, 63), TraitGenome.empty(), 1, 0);
        assertTrue(phenotype(d, hot, roots(5000, 0, 0), Environment.reference(15).with("agricrops:temperature", .4, "test")).fit < 1);
        assertTrue(phenotype(d, hot, roots(5000, 0, 0), Environment.reference(0)).hardBlocked);
    }
    @Test public void unavailableFactorFailsClosedInsteadOfInventingAValue() {
        CropDefinition d = crop("a", rules().requirement(new Requirement("test:pollution", 0, 1, 1, false)).build());
        CropActions.Step result = CropActions.step(d, CropState.fresh(d, SeedOrigin.WILD), roots(1000, 0, 0), Environment.reference(15), Collections.singletonMap(d.id(), d), Collections.emptyMap());
        assertEquals(CropActions.Reason.FACTOR_MISSING, result.reason); assertEquals(1000, result.roots.cells.get(0).water());
    }
    @Test public void waterAndFertilityResponsesDependOnRealStock() {
        CropDefinition d = crop("a", rules().build());
        CropState s = state(d, StandardGenome.neutral().with(StandardStat.WATER_AFFINITY, 63).with(StandardStat.FERTILITY_RESPONSE, 63), TraitGenome.empty(), 1, 0);
        assertTrue(phenotype(d, s, roots(8000, 0, 0), Environment.reference(15)).rate > phenotype(d, s, roots(1000, 0, 0), Environment.reference(15)).rate);
    }
    @Test public void rootsExpandWithPhaseAndDeepWaterIsNotInitiallyAvailable() {
        CropDefinition d = crop("a", rules().build()); CropState s = CropState.fresh(d, SeedOrigin.WILD); RootZone r = roots(0, 0, 1000);
        Map<String, CropDefinition> crops = Collections.singletonMap(d.id(), d);
        assertEquals(CropActions.Reason.WATER, CropActions.step(d, s, r, Environment.reference(15), crops, Collections.emptyMap()).reason);
        CropActions.Step flowering = CropActions.step(d, s.at(200), r, Environment.reference(15), crops, Collections.emptyMap());
        assertTrue(flowering.crop.progress() > 200); assertEquals(0, flowering.roots.cells.get(0).water()); assertTrue(flowering.roots.cells.get(2).water() < 1000);
    }
    @Test public void compostReleaseConservesNutrientsAndDrainageConservesTransfers() {
        SoilProfile p = new SoilProfile("test:soil", 10000, 10000, 2, 100, 10, 1000, .9);
        SoilCell input = new SoilCell(p.block, 9500, 2000, 500); SoilCell released = p.tick(input, 0, 1);
        assertEquals(2500, released.nutrients() + released.organic()); assertEquals(9498, released.water());
        RootZone r = new RootZone(Arrays.asList(input, new SoilCell(p.block, 0, 0)), Arrays.asList(p, p)).drain();
        assertEquals(9500, r.cells.get(0).water() + r.cells.get(1).water()); assertEquals(100, r.cells.get(1).water());
    }
    @Test public void saturatedClayCanLimitAerationAndProfileReplacementNeverMintsStock() {
        SoilProfile p = new SoilProfile("test:soil", 10000, 10000, 0, 0, 0, 1000, .7);
        assertTrue(p.aeration(new SoilCell(p.block, 10000, 1000)) < p.aeration(new SoilCell(p.block, 5000, 1000)));
        SoilCell changed = new SoilCell(p.block, 10000, 10000, 10000).reconcile("test:sand");
        assertEquals(0, changed.water() + changed.nutrients() + changed.organic());
    }
    @Test public void foundationIsRequiredEvenWhenRegressionDropsTheBodyProduct() {
        CropDefinition a = crop("a", rules().foundation(2, "test:clay").build()), b = crop("b", rules().build());
        CropState s = CropState.plant(new Specimen(a.id(), StandardGenome.neutral(), SeedOrigin.BRED, ProductMix.single(b.id()), TraitGenome.empty(), 1, 1), 0);
        Map<String, CropDefinition> crops = new HashMap<>(); crops.put(a.id(), a); crops.put(b.id(), b);
        assertEquals(CropActions.Reason.FOUNDATION, CropActions.step(a, s, roots(1000, 1000, 1000), Environment.reference(15), crops, Collections.emptyMap()).reason);
    }
    @Test public void singleUnitMixturesEventuallyEmitBothAndNeverMultiplyTheBudget() {
        Map<String, Integer> weights = new TreeMap<>(); weights.put("test:a", 3); weights.put("test:b", 1); ProductMix mix = new ProductMix(weights);
        Map<String, Integer> credit = Collections.emptyMap(); int a = 0, b = 0;
        for (int i = 0; i < 40; i++) { ProductMix.Allocation allocation = mix.allocate(1, credit); a += allocation.output.getOrDefault("test:a", 0); b += allocation.output.getOrDefault("test:b", 0); credit = allocation.nextCredit; }
        assertEquals(30, a); assertEquals(10, b);
    }
    @Test public void hybridProductionRequiresBothSourcePricesAndNoFreeImmediateSecondHarvest() {
        CropDefinition a = crop("a", rules().build()), b = crop("b", rules().build()); Map<String, CropDefinition> crops = new HashMap<>(); crops.put(a.id(), a); crops.put(b.id(), b);
        Map<String, Integer> weights = new TreeMap<>(); weights.put(a.id(), 1); weights.put(b.id(), 1);
        CropState s = CropState.plant(new Specimen(a.id(), StandardGenome.neutral(), SeedOrigin.BRED, new ProductMix(weights), TraitGenome.empty(), 1, 1), 0);
        RootZone r = roots(5000, 5000, 5000); int initial = r.cells.stream().mapToInt(SoilCell::water).sum();
        for (int i = 0; i < 30 && !s.funded(); i++) { CropActions.Step step = CropActions.step(a, s, r, Environment.reference(15), crops, Collections.emptyMap()); s = step.crop; r = step.roots; }
        assertTrue(s.funded()); assertEquals(800, initial - r.cells.stream().mapToInt(SoilCell::water).sum());
        CropActions.Harvest result = CropActions.harvest(a, s); assertEquals(2, result.count); assertEquals(1, result.products.get(a.id()).intValue()); assertEquals(1, result.products.get(b.id()).intValue());
        assertEquals(0, CropActions.harvest(a, result.crop).count);
    }
    @Test public void clippingsPreserveAncestryButNeverDevelopmentFundingOrFreshStatus() {
        CropDefinition d = crop("a", rules().build()); Specimen parent = Specimen.fresh(d, SeedOrigin.WILD, 27); Specimen clipping = parent.clipping();
        CropState s = CropState.plant(clipping, 400); assertEquals(27, clipping.lineage); assertFalse(clipping.freshDonor()); assertEquals(0, s.progress()); assertNull(s.plan());
        CropActions.Step step = CropActions.step(d, s, roots(1000, 0, 0), Environment.reference(15), Collections.singletonMap(d.id(), d), Collections.emptyMap());
        assertEquals(380, step.crop.rooting()); assertEquals(0, step.crop.progress()); assertEquals(980, step.roots.cells.get(0).water()); assertFalse(Breeding.window(d, step.crop.at(200), true));
    }
    private TraitDefinition frost() { return new TraitDefinition("test:frost", "agricrops:climate", TraitDefinition.Effect.COLD_TOLERANCE, "agricrops:temperature", .6, 2, 3, "test.frost"); }
    private TraitGenome alleles(TraitDefinition type, int stable, boolean fixed) { return new TraitGenome(Collections.singletonMap(type.locus, new TraitGenome.Pair(type.id, type.id, stable, fixed))); }
    @Test public void introducedTraitsExpressAndCompeteInOneLocus() {
        TraitDefinition frost = frost(); Map<String, TraitDefinition> traits = Collections.singletonMap(frost.id, frost);
        CropDefinition d = crop("a", rules().requirement(new Requirement("agricrops:temperature", .5, 1, 1, false)).build()); RootZone r = roots(1000, 0, 0); Environment e = Environment.reference(15).with("agricrops:temperature", 0, "test");
        CropState carrier = state(d, StandardGenome.neutral(), alleles(frost, 0, false), 1, 0), empty = CropState.fresh(d, SeedOrigin.WILD);
        assertTrue(Phenotype.evaluate(d, carrier, r, e, traits).fit > Phenotype.evaluate(d, empty, r, e, traits).fit);
    }
    @Test public void distinctLinesCanFixTraitsClonesCannotAndFreshRecoveryReopensThePath() {
        TraitDefinition frost = frost(); Map<String, TraitDefinition> traits = Collections.singletonMap(frost.id, frost); CropDefinition d = crop("a", rules().build()); Map<String, CropDefinition> crops = Collections.singletonMap(d.id(), d);
        CropState a = state(d, StandardGenome.neutral(), alleles(frost, 2, false), 1, 200), b = state(d, StandardGenome.neutral(), alleles(frost, 2, false), 2, 200);
        Specimen fixed = Breeding.cross(a, b, false, crops, traits, Collections.emptyList(), new Random(1), 3); assertTrue(fixed.traits.loci().get(frost.locus).fixed);
        Specimen cloned = Breeding.cross(a, a, false, crops, traits, Collections.emptyList(), new Random(1), 3); assertFalse(cloned.traits.loci().get(frost.locus).fixed);
        CropState fresh = CropState.plant(Specimen.fresh(d, SeedOrigin.CRAFTED, 4), 0).at(200);
        Specimen locked = Breeding.cross(CropState.plant(fixed, 0).at(200), fresh, false, crops, traits, Collections.emptyList(), new Random(1), 5); assertTrue(locked.traits.loci().get(frost.locus).fixed);
        Specimen recovered = Breeding.cross(CropState.plant(fixed, 0).at(200), fresh, true, crops, traits, Collections.emptyList(), new Random(1), 5); assertTrue(recovered.traits.loci().isEmpty());
    }
    @Test public void offspringCanRegressAndNeverBecomePurelyMonotonicUpgrades() {
        CropDefinition d = crop("a", rules().build()); Map<String, CropDefinition> crops = Collections.singletonMap(d.id(), d); boolean down = false, up = false;
        CropState s = state(d, StandardGenome.neutral().with(StandardStat.GROWTH, 30).with(StandardStat.MUTATION_CHANCE, 63), TraitGenome.empty(), 1, 200);
        for (int seed = 0; seed < 200; seed++) { int value = Breeding.cross(s, s, false, crops, Collections.emptyMap(), Collections.emptyList(), new Random(seed), seed + 3).genes.get(StandardStat.GROWTH); down |= value < 30; up |= value > 30; }
        assertTrue(down && up);
    }
    @Test(expected = IllegalArgumentException.class) public void wrongParentWindowCannotBeBypassedByGoodGenes() {
        CropDefinition d = crop("a", rules().build()); CropState s = CropState.fresh(d, SeedOrigin.WILD);
        Breeding.cross(s, s.at(200), false, Collections.singletonMap(d.id(), d), Collections.emptyMap(), Collections.emptyList(), new Random(1), 3);
    }
    @Test public void competingFixedAllelesRejectRatherThanStack() {
        TraitDefinition frost = frost(), heat = new TraitDefinition("test:heat", frost.locus, TraitDefinition.Effect.HEAT_TOLERANCE, frost.factor, .6, 2, 3, "test.heat");
        Map<String, TraitDefinition> traits = new HashMap<>(); traits.put(frost.id, frost); traits.put(heat.id, heat); CropDefinition d = crop("a", rules().build());
        CropState a = state(d, StandardGenome.neutral(), alleles(frost, 3, true), 1, 200), b = state(d, StandardGenome.neutral(), alleles(heat, 3, true), 2, 200);
        try { Breeding.cross(a, b, false, Collections.singletonMap(d.id(), d), traits, Collections.emptyList(), new Random(1), 3); fail("Stacked competing fixed paths"); }
        catch (IllegalArgumentException expected) { assertEquals("fixed_conflict", expected.getMessage()); }
    }
    @Test public void differentAuthoredDonorAndRecipientPhasesAreRespected() {
        CropDefinition d = crop("a", rules().windows(GrowthPhase.VEGETATIVE, GrowthPhase.FRUITING).build()); CropState s = CropState.fresh(d, SeedOrigin.WILD);
        assertTrue(Breeding.window(d, s.at(100), true)); assertFalse(Breeding.window(d, s.at(100), false)); assertTrue(Breeding.window(d, s.at(300), false));
        assertNotNull(Breeding.cross(s.at(100), s.at(300), false, Collections.singletonMap(d.id(), d), Collections.emptyMap(), Collections.emptyList(), new Random(1), 5));
    }
    @Test public void rootDrainDoesNotPullNegativeWaterFromAnOverfullLowerProfile() {
        SoilProfile p = new SoilProfile("test:soil", 1000, 1000, 0, 100, 0, 100, .9);
        RootZone zone = new RootZone(Arrays.asList(new SoilCell(p.block, 1000, 0), new SoilCell(p.block, 2000, 0)), Arrays.asList(p, p));
        RootZone drained = zone.drain(); assertEquals(1000, drained.cells.get(0).water()); assertEquals(1900, drained.cells.get(1).water());
    }
    @Test public void hybridBodyCannotBypassAnActiveSourceHardHabitatGate() {
        CropDefinition body = crop("easy", rules().build());
        CropDefinition gated = crop("rare", rules().requirement(new Requirement("agricrops:height", 100, 255, 0, true)).build());
        Map<String, CropDefinition> crops = new HashMap<>(); crops.put(body.id(), body); crops.put(gated.id(), gated);
        Map<String, Integer> mix = new TreeMap<>(); mix.put(body.id(), 1); mix.put(gated.id(), 1);
        CropState state = CropState.plant(new Specimen(body.id(), StandardGenome.neutral(), SeedOrigin.BRED, new ProductMix(mix), TraitGenome.empty(), 5, 1), 0);
        RootZone soil = roots(5000, 5000, 5000);
        CropActions.Step blocked = CropActions.step(body, state, soil, Environment.reference(15), crops, Collections.emptyMap());
        assertEquals(CropActions.Reason.ENVIRONMENT, blocked.reason); assertSame(state, blocked.crop); assertEquals(5000, blocked.roots.cells.get(0).water());
        CropActions.Step allowed = CropActions.step(body, state, soil, Environment.reference(15).with("agricrops:height", 150, "test world Y"), crops, Collections.emptyMap());
        assertTrue(allowed.crop.progress() > 0); assertTrue(allowed.crop.paidWater() > 0);
    }
}
