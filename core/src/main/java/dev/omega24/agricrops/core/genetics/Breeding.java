package dev.omega24.agricrops.core.genetics;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.field.CropState;
import java.util.*;

/** Pure paid-action preparation: the world adapter owns permission, source debits and commit. */
public final class Breeding {
    public static final class Unlock {
        public final String donor, recipient, trait;
        public Unlock(String donor, String recipient, String trait) {
            this.donor = CropDefinition.identifier(donor); this.recipient = CropDefinition.identifier(recipient); this.trait = CropDefinition.identifier(trait);
        }
        public boolean matches(Specimen first, Specimen second) {
            return (first.products.weights().containsKey(donor) && second.products.weights().containsKey(recipient))
                    || (first.products.weights().containsKey(recipient) && second.products.weights().containsKey(donor));
        }
    }
    private Breeding() {}
    public static boolean window(CropDefinition definition, CropState state, boolean donor) {
        return definition.rules().breedable && state.rooting() == 0 && definition.timeline().phaseAt(state.progress()) == (donor ? definition.rules().donor : definition.rules().recipient);
    }
    public static Specimen cross(CropState donor, CropState recipient, boolean backcross, Map<String, CropDefinition> crops,
                                 Map<String, TraitDefinition> traits, List<Unlock> unlocks, Random random, long lineage) {
        CropDefinition first = crops.get(donor.crop()), second = crops.get(recipient.crop());
        if (first == null || second == null || !window(first, donor, true) || !window(second, recipient, false)) throw new IllegalArgumentException("parent_window");
        if (!first.rules().family.equals(second.rules().family)) throw new IllegalArgumentException("compatibility");
        Specimen a = donor.specimen(), b = recipient.specimen();
        if (backcross && !a.freshDonor() && !b.freshDonor()) throw new IllegalArgumentException("fresh_donor");
        int mutation = (a.genes.get(StandardStat.MUTATION_CHANCE) + b.genes.get(StandardStat.MUTATION_CHANCE)) / 2;
        StandardGenome genes = StandardGenome.neutral();
        int variation = 1 + mutation / 16;
        for (StandardStat stat : StandardStat.values()) {
            int base = stat.isDirectional() ? (random.nextBoolean() ? a.genes.get(stat) : b.genes.get(stat))
                    : (a.genes.get(stat) + b.genes.get(stat)) / 2;
            int changed = base + random.nextInt(variation * 2 + 1) - variation;
            genes = genes.with(stat, Math.max(stat.minimum(), Math.min(StandardStat.MAX, changed)));
        }
        ProductMix products;
        if (random.nextInt(20) < 1 + mutation / 16) products = random.nextBoolean() ? a.products : b.products;
        else {
            Map<String, Integer> weights = new TreeMap<>();
            for (ProductMix source : Arrays.asList(a.products, b.products)) source.weights().forEach((id, weight) -> weights.put(id,
                    weights.getOrDefault(id, 0) + Math.max(1, (int) Math.round(500.0 * weight / source.total()))));
            if (weights.size() > ProductMix.MAX_COMPONENTS) throw new IllegalArgumentException("component_cap");
            if (weights.size() > 1) {
                List<String> ids = new ArrayList<>(weights.keySet()); String from = ids.get(random.nextInt(ids.size())), to = ids.get(random.nextInt(ids.size()));
                int move = Math.min(weights.get(from) - 1, random.nextInt(variation * 10 + 1));
                weights.put(from, weights.get(from) - move); weights.put(to, weights.get(to) + move);
            }
            products = new ProductMix(weights);
        }
        Map<Integer, String> foundations = new TreeMap<>(second.rules().foundations);
        for (String id : products.weights().keySet()) {
            CropDefinition source = crops.get(id);
            if (source == null || !source.rules().breedable || !source.rules().family.equals(second.rules().family) || !source.soil().equals(second.soil())) throw new IllegalArgumentException("source_permission");
            source.rules().foundations.forEach((depth, block) -> { String previous = foundations.put(depth, block); if (previous != null && !previous.equals(block)) throw new IllegalArgumentException("foundation_conflict"); });
        }
        Map<String, TraitGenome.Pair> inherited = new TreeMap<>();
        for (String locus : second.rules().loci) {
            TraitGenome.Pair x = a.traits.loci().get(locus), y = b.traits.loci().get(locus);
            if (backcross && ((x != null && x.fixed) || (y != null && y.fixed))) {
                TraitGenome.Pair fresh = a.freshDonor() ? a.traits.loci().get(locus) : b.traits.loci().get(locus);
                if (fresh != null) inherited.put(locus, fresh);
                continue; // A paid fresh-line backcross opens this competing locus on the offspring.
            }
            if (x != null && x.fixed && y != null && y.fixed && !x.first.equals(y.first)) throw new IllegalArgumentException("fixed_conflict");
            if (x != null && x.fixed) { inherited.put(locus, x); continue; }
            if (y != null && y.fixed) { inherited.put(locus, y); continue; }
            String alleleX = x == null ? "" : x.donate(random), alleleY = y == null ? "" : y.donate(random);
            if (alleleX.isEmpty() && alleleY.isEmpty()) continue;
            int stability = x != null && y != null ? Math.min(10, Math.min(x.generations, y.generations) + (a.lineage != b.lineage ? 1 : 0)) : 0;
            TraitDefinition type = traits.get(alleleX.isEmpty() ? alleleY : alleleX);
            if (type == null) throw new IllegalArgumentException("unknown_trait");
            boolean fixed = !alleleX.isEmpty() && alleleX.equals(alleleY) && stability >= type.generations;
            inherited.put(locus, new TraitGenome.Pair(alleleX, alleleY, stability, fixed));
        }
        if (!backcross) for (Unlock unlock : unlocks) {
            TraitDefinition type = traits.get(unlock.trait);
            if (type == null) throw new IllegalArgumentException("unknown_trait");
            if (unlock.matches(a, b) && second.rules().loci.contains(type.locus) && !inherited.containsKey(type.locus)) {
                inherited.put(type.locus, new TraitGenome.Pair(type.id, type.id, 0, false));
            }
        }
        TraitGenome genome = new TraitGenome(inherited); genome.expressed(traits);
        return new Specimen(second.id(), genes, SeedOrigin.BRED, products, genome, lineage, Math.min(1000000, Math.max(a.generation, b.generation) + 1));
    }
}
