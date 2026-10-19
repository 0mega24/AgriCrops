package dev.omega24.agricrops.core.genetics;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Eight immutable standard values. Locked loci/inheritance are separate future contracts. */
public final class StandardGenome {
    private final Map<StandardStat, Integer> values;

    public StandardGenome(Map<StandardStat, Integer> input) {
        Objects.requireNonNull(input, "input");
        EnumMap<StandardStat, Integer> copy = new EnumMap<>(StandardStat.class);
        for (StandardStat stat : StandardStat.values()) {
            Integer value = input.get(stat);
            if (value == null) {
                throw new IllegalArgumentException("Missing standard stat: " + stat.id());
            }
            stat.validate(value);
            copy.put(stat, value);
        }
        this.values = Collections.unmodifiableMap(copy);
    }

    public static StandardGenome neutral() {
        EnumMap<StandardStat, Integer> values = new EnumMap<>(StandardStat.class);
        for (StandardStat stat : StandardStat.values()) values.put(stat, 0);
        return new StandardGenome(values);
    }

    public int get(StandardStat stat) { return values.get(Objects.requireNonNull(stat, "stat")); }
    public Map<StandardStat, Integer> values() { return values; }

    public StandardGenome with(StandardStat stat, int value) {
        Objects.requireNonNull(stat, "stat").validate(value);
        EnumMap<StandardStat, Integer> copy = new EnumMap<>(values);
        copy.put(stat, value);
        return new StandardGenome(copy);
    }

    @Override public boolean equals(Object other) {
        return other instanceof StandardGenome && values.equals(((StandardGenome) other).values);
    }

    @Override public int hashCode() { return values.hashCode(); }
}
