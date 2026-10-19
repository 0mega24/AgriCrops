package dev.omega24.agricrops.core.growth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/** Reference active growth ticks, before environment/genetic/resource rate modifiers. */
public final class GrowthTimeline {
    public static final class Segment {
        private final GrowthPhase phase;
        private final long ticks;

        public Segment(GrowthPhase phase, long ticks) {
            this.phase = Objects.requireNonNull(phase, "phase");
            if (phase == GrowthPhase.MATURE || ticks <= 0) {
                throw new IllegalArgumentException("Development segments need a nonterminal phase and positive ticks");
            }
            this.ticks = ticks;
        }

        public GrowthPhase phase() { return phase; }
        public long ticks() { return ticks; }
    }

    private final List<Segment> segments;
    private final long totalTicks;

    public GrowthTimeline(List<Segment> input) {
        Objects.requireNonNull(input, "input");
        if (input.isEmpty()) throw new IllegalArgumentException("At least one phase is required");
        List<Segment> copy = new ArrayList<>();
        EnumSet<GrowthPhase> seen = EnumSet.noneOf(GrowthPhase.class);
        long total = 0;
        for (Segment segment : input) {
            Objects.requireNonNull(segment, "segment");
            if (!seen.add(segment.phase())) throw new IllegalArgumentException("Duplicate phase: " + segment.phase());
            total = Math.addExact(total, segment.ticks());
            copy.add(segment);
        }
        segments = Collections.unmodifiableList(copy);
        totalTicks = total;
    }

    public long totalTicks() { return totalTicks; }
    public List<Segment> segments() { return segments; }

    public GrowthPhase phaseAt(long elapsedActiveGrowthTicks) {
        if (elapsedActiveGrowthTicks < 0) throw new IllegalArgumentException("Elapsed growth cannot be negative");
        long remaining = elapsedActiveGrowthTicks;
        for (Segment segment : segments) {
            if (remaining < segment.ticks()) return segment.phase();
            remaining -= segment.ticks();
        }
        return GrowthPhase.MATURE;
    }
}
