package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.genetics.*;
import java.util.*;
import java.util.Objects;

/** Persisted specimen and development, deliberately separate from position-owned soil. */
public final class CropState {
    private final Specimen specimen;
    private final long progress, cycleStart, rooting;
    private final ProductionPlan plan;
    private final int paidWater, paidNutrients;
    private final Map<String, Integer> credit;

    public CropState(String crop, StandardGenome genome, SeedOrigin origin, long progress) {
        this(new Specimen(crop, genome, origin, ProductMix.single(crop), TraitGenome.empty(), 0, 0), progress, 0, 0, null, 0, 0, Collections.emptyMap());
    }
    public CropState(Specimen specimen, long progress, long cycleStart, long rooting, ProductionPlan plan, int paidWater, int paidNutrients, Map<String, Integer> credit) {
        this.specimen = Objects.requireNonNull(specimen);
        if (progress < 0 || progress > 288000 || cycleStart < 0 || cycleStart > progress || rooting < 0 || rooting > 12000 || paidWater < 0 || paidNutrients < 0
                || (plan == null && (paidWater != 0 || paidNutrients != 0)) || (plan != null && (paidWater > plan.water || paidNutrients > plan.nutrients))) throw new IllegalArgumentException("Invalid crop development/funding");
        specimen.products.allocate(1, credit); // Validate saved scheduling state, including zero-sum.
        if (plan != null && (!specimen.products.weights().keySet().equals(plan.nextCredit.keySet()) || !specimen.products.weights().keySet().containsAll(plan.products.keySet()))) throw new IllegalArgumentException("Plan/specimen mismatch");
        this.progress = progress; this.cycleStart = cycleStart; this.rooting = rooting; this.plan = plan; this.paidWater = paidWater; this.paidNutrients = paidNutrients;
        this.credit = Collections.unmodifiableMap(new TreeMap<>(credit));
    }

    public static CropState fresh(CropDefinition definition, SeedOrigin origin) {
        return new CropState(definition.id(), definition.baseline(), origin, 0);
    }

    public static CropState plant(Specimen specimen, int rootingTicks) {
        return new CropState(specimen, 0, 0, rootingTicks, null, 0, 0, Collections.emptyMap());
    }
    public String crop() { return specimen.body; }
    public StandardGenome genome() { return specimen.genes; }
    public SeedOrigin origin() { return specimen.origin; }
    public Specimen specimen() { return specimen; }
    public long progress() { return progress; }
    public long cycleStart() { return cycleStart; }
    public long rooting() { return rooting; }
    public ProductionPlan plan() { return plan; }
    public int paidWater() { return paidWater; }
    public int paidNutrients() { return paidNutrients; }
    public Map<String, Integer> credit() { return credit; }
    public boolean funded() { return plan != null && paidWater == plan.water && paidNutrients == plan.nutrients; }
    public CropState at(long progress) { return new CropState(specimen, progress, Math.min(cycleStart, progress), rooting, plan, paidWater, paidNutrients, credit); }
    public CropState planned(ProductionPlan plan) { return new CropState(specimen, progress, cycleStart, rooting, plan, paidWater, paidNutrients, credit); }
    public CropState advance(long progress, long rooting, int water, int nutrients) { return new CropState(specimen, progress, cycleStart, rooting, plan, water, nutrients, credit); }
    public CropState harvested(long start) { return new CropState(specimen, start, start, 0, null, 0, 0, plan.nextCredit); }
}
