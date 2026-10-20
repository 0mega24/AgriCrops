package dev.omega24.agricrops.core.genetics;

import dev.omega24.agricrops.core.content.CropDefinition;
import java.util.*;

/** Two alleles per named locus; fixation is a local closed path, never a global player unlock. */
public final class TraitGenome {
    public static final class Pair {
        public final String first, second;
        public final int generations;
        public final boolean fixed;
        public Pair(String first, String second, int generations, boolean fixed) {
            if (!first.isEmpty()) CropDefinition.identifier(first); if (!second.isEmpty()) CropDefinition.identifier(second);
            if ((first.isEmpty() && second.isEmpty()) || generations < 0 || generations > 10 || (fixed && (first.isEmpty() || !first.equals(second)))) throw new IllegalArgumentException("Invalid trait alleles");
            this.first = first; this.second = second; this.generations = generations; this.fixed = fixed;
        }
        public String donate(Random random) { return random.nextBoolean() ? first : second; }
    }
    private final Map<String, Pair> loci;
    public TraitGenome(Map<String, Pair> input) {
        if (input.size() > 8) throw new IllegalArgumentException("Too many loci");
        Map<String, Pair> copy = new TreeMap<>(); input.forEach((id, pair) -> copy.put(CropDefinition.identifier(id), Objects.requireNonNull(pair)));
        loci = Collections.unmodifiableMap(copy);
    }
    public static TraitGenome empty() { return new TraitGenome(Collections.emptyMap()); }
    public Map<String, Pair> loci() { return loci; }
    public Map<String, Double> expressed(Map<String, TraitDefinition> definitions) {
        Map<String, Double> result = new TreeMap<>();
        loci.forEach((locus, pair) -> {
            TraitDefinition first = pair.first.isEmpty() ? null : definitions.get(pair.first);
            TraitDefinition second = pair.second.isEmpty() ? null : definitions.get(pair.second);
            if ((!pair.first.isEmpty() && first == null) || (!pair.second.isEmpty() && second == null)
                    || (first != null && !locus.equals(first.locus)) || (second != null && !locus.equals(second.locus))) throw new IllegalArgumentException("Unknown or incompatible trait locus " + locus);
            if (first != null && (second == null || first.dominance > second.dominance || first.id.equals(second.id))) result.put(first.id, first.strength);
            else if (second != null && (first == null || second.dominance > first.dominance)) result.put(second.id, second.strength);
            else if (first != null) { result.put(first.id, first.strength / 2); result.put(second.id, second.strength / 2); }
        });
        return result;
    }
}
