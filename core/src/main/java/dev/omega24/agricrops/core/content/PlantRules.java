package dev.omega24.agricrops.core.content;

import dev.omega24.agricrops.core.environment.Requirement;
import dev.omega24.agricrops.core.growth.GrowthPhase;
import java.util.*;

/** Small immutable body profile; the same builder is used by Java defaults and CT. */
public final class PlantRules {
    public final String family, visual;
    public final int seedMeta, productMeta;
    public final String seedNbt, productNbt;
    public final int roots, harvestUnits, rootingTicks, reproductionWater, reproductionNutrients;
    public final boolean clippable, breedable;
    public final GrowthPhase donor, recipient;
    public final List<Requirement> requirements;
    public final Map<Integer, String> foundations;
    public final Set<String> loci;
    private PlantRules(Builder b) {
        family = CropDefinition.identifier(b.family);
        if (b.seedMeta < 0 || b.seedMeta > 32767 || b.productMeta < 0 || b.productMeta > 32767 || b.seedNbt.length() > 4096 || b.productNbt.length() > 4096) throw new IllegalArgumentException("Invalid item template");
        seedMeta = b.seedMeta; productMeta = b.productMeta; seedNbt = b.seedNbt; productNbt = b.productNbt;
        if (!Arrays.asList("wheat", "beetroot", "carrot").contains(b.visual)) throw new IllegalArgumentException("Unsupported visual profile " + b.visual);
        if (b.roots < 1 || b.roots > 3 || b.harvestUnits < 1 || b.harvestUnits > 4 || b.rootingTicks < 20 || b.rootingTicks > 12000
                || b.reproductionWater < 1 || b.reproductionWater > 10000 || b.reproductionNutrients < 1 || b.reproductionNutrients > 10000) throw new IllegalArgumentException("Invalid plant body bounds");
        if (b.donor == GrowthPhase.GERMINATION || b.recipient == GrowthPhase.GERMINATION) throw new IllegalArgumentException("Seedlings cannot reproduce");
        visual = b.visual; roots = b.roots; harvestUnits = b.harvestUnits; rootingTicks = b.rootingTicks;
        reproductionWater = b.reproductionWater; reproductionNutrients = b.reproductionNutrients;
        clippable = b.clippable; breedable = b.breedable; donor = b.donor; recipient = b.recipient;
        if (b.requirements.size() > 16 || b.loci.size() > 8) throw new IllegalArgumentException("Plant profile is too large");
        requirements = Collections.unmodifiableList(new ArrayList<>(b.requirements.values()));
        foundations = Collections.unmodifiableMap(new TreeMap<>(b.foundations));
        loci = Collections.unmodifiableSet(new TreeSet<>(b.loci));
    }
    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private String family = "agricrops:field", visual = "wheat";
        private int seedMeta, productMeta;
        private String seedNbt = "", productNbt = "";
        private int roots = 1, harvestUnits = 1, rootingTicks = 400, reproductionWater = 100, reproductionNutrients = 100;
        private boolean clippable = true, breedable = true;
        private GrowthPhase donor = GrowthPhase.FLOWERING, recipient = GrowthPhase.FLOWERING;
        private final Map<String, Requirement> requirements = new LinkedHashMap<>();
        private final Map<Integer, String> foundations = new TreeMap<>();
        private final Set<String> loci = new TreeSet<>(Arrays.asList("agricrops:climate", "agricrops:roots", "agricrops:moisture"));
        public Builder family(String id) { family = id; return this; }
        public Builder itemMetadata(int seed, int product) { seedMeta = seed; productMeta = product; return this; }
        public Builder itemNbt(String seed, String product) { seedNbt = Objects.requireNonNull(seed); productNbt = Objects.requireNonNull(product); return this; }
        public Builder visual(String id) { visual = id; return this; }
        public Builder roots(int depth) { roots = depth; return this; }
        public Builder harvestUnits(int count) { harvestUnits = count; return this; }
        public Builder rootingTicks(int ticks) { rootingTicks = ticks; return this; }
        public Builder clippable(boolean value) { clippable = value; return this; }
        public Builder breedable(boolean value) { breedable = value; return this; }
        public Builder windows(GrowthPhase donor, GrowthPhase recipient) { this.donor = Objects.requireNonNull(donor); this.recipient = Objects.requireNonNull(recipient); return this; }
        public Builder reproductionCost(int water, int nutrients) { reproductionWater = water; reproductionNutrients = nutrients; return this; }
        public Builder requirement(Requirement requirement) { requirements.put(requirement.factor, requirement); return this; }
        public Builder foundation(int depth, String block) { if (depth < 1 || depth > 3) throw new IllegalArgumentException("Foundation depth must be 1..3"); foundations.put(depth, CropDefinition.identifier(block)); return this; }
        public Builder loci(String[] ids) { loci.clear(); for (String id : ids) loci.add(CropDefinition.identifier(id)); return this; }
        public PlantRules build() { return new PlantRules(this); }
    }
}
