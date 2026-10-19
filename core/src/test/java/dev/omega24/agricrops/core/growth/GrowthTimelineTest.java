package dev.omega24.agricrops.core.growth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrowthTimelineTest {
    private GrowthTimeline timeline() {
        return new GrowthTimeline(Arrays.asList(
                new GrowthTimeline.Segment(GrowthPhase.GERMINATION, 20),
                new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, 40),
                new GrowthTimeline.Segment(GrowthPhase.FLOWERING, 40)));
    }

    @Test public void reproductiveWindowHasExactActiveTimeBoundaries() {
        GrowthTimeline growth = timeline();
        assertEquals(GrowthPhase.GERMINATION, growth.phaseAt(0));
        assertEquals(GrowthPhase.GERMINATION, growth.phaseAt(19));
        assertEquals(GrowthPhase.VEGETATIVE, growth.phaseAt(20));
        assertEquals(GrowthPhase.VEGETATIVE, growth.phaseAt(59));
        assertEquals(GrowthPhase.FLOWERING, growth.phaseAt(60));
        assertEquals(GrowthPhase.FLOWERING, growth.phaseAt(99));
        assertEquals(GrowthPhase.MATURE, growth.phaseAt(100));
        assertEquals(GrowthPhase.MATURE, growth.phaseAt(Long.MAX_VALUE));
        assertEquals(100, growth.totalTicks());
    }

    @Test public void editingTheSourceCannotChangeARegisteredSchedule() {
        List<GrowthTimeline.Segment> source = new ArrayList<>();
        source.add(new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, 40));
        GrowthTimeline growth = new GrowthTimeline(source);
        source.clear();
        assertEquals(GrowthPhase.VEGETATIVE, growth.phaseAt(0));
        assertEquals(40, growth.totalTicks());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeElapsedTimeIsRejected() { timeline().phaseAt(-1); }

    @Test(expected = IllegalArgumentException.class)
    public void zeroLengthWindowsAreRejected() { new GrowthTimeline.Segment(GrowthPhase.FLOWERING, 0); }

    @Test(expected = ArithmeticException.class)
    public void authoredDurationsCannotOverflowIntoInstantMaturity() {
        new GrowthTimeline(Arrays.asList(
                new GrowthTimeline.Segment(GrowthPhase.GERMINATION, Long.MAX_VALUE),
                new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, 1)));
    }
}
