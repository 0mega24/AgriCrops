package dev.omega24.agricrops.core.genetics;

import dev.omega24.agricrops.core.content.CropDefinition;
import java.util.*;

/** Bounded active components, not an ancestry tree. Credit is scheduling, never production funding. */
public final class ProductMix {
    public static final int MAX_COMPONENTS = 4;
    private final Map<String, Integer> weights;
    public ProductMix(Map<String, Integer> input) {
        if (input.isEmpty() || input.size() > MAX_COMPONENTS) throw new IllegalArgumentException("A mix needs 1..4 active components");
        Map<String, Integer> copy = new TreeMap<>(); int divisor = 0;
        for (Map.Entry<String, Integer> entry : input.entrySet()) {
            CropDefinition.identifier(entry.getKey());
            if (entry.getValue() == null || entry.getValue() < 1 || entry.getValue() > 10000) throw new IllegalArgumentException("Invalid product weight");
            divisor = gcd(divisor, entry.getValue()); copy.put(entry.getKey(), entry.getValue());
        }
        final int common = divisor; copy.replaceAll((id, weight) -> weight / common);
        weights = Collections.unmodifiableMap(copy);
    }
    private static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
    public static ProductMix single(String crop) { return new ProductMix(Collections.singletonMap(crop, 1)); }
    public Map<String, Integer> weights() { return weights; }
    public int total() { return weights.values().stream().mapToInt(Integer::intValue).sum(); }
    public Allocation allocate(int units, Map<String, Integer> previous) {
        if (units < 1 || units > 8 || !weights.keySet().containsAll(previous.keySet())) throw new IllegalArgumentException("Invalid production allocation");
        Map<String, Integer> credit = new TreeMap<>(), output = new TreeMap<>();
        for (String id : weights.keySet()) { int value = previous.getOrDefault(id, 0); if (Math.abs((long) value) > 40000) throw new IllegalArgumentException("Invalid allocation credit"); credit.put(id, value); }
        if (credit.values().stream().mapToInt(Integer::intValue).sum() != 0) throw new IllegalArgumentException("Allocation credit must sum to zero");
        for (int i = 0; i < units; i++) {
            String selected = null;
            for (String id : weights.keySet()) { credit.put(id, credit.get(id) + weights.get(id)); if (selected == null || credit.get(id) > credit.get(selected)) selected = id; }
            credit.put(selected, credit.get(selected) - total()); output.put(selected, output.getOrDefault(selected, 0) + 1);
        }
        return new Allocation(output, credit);
    }
    public static final class Allocation {
        public final Map<String, Integer> output, nextCredit;
        public Allocation(Map<String, Integer> output, Map<String, Integer> nextCredit) {
            this.output = Collections.unmodifiableMap(new TreeMap<>(output)); this.nextCredit = Collections.unmodifiableMap(new TreeMap<>(nextCredit));
        }
    }
    @Override public boolean equals(Object other) { return other instanceof ProductMix && weights.equals(((ProductMix) other).weights); }
    @Override public int hashCode() { return weights.hashCode(); }
}
