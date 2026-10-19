package dev.omega24.agricrops.core.genetics;

import java.util.EnumMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class StandardGenomeTest {
    @Test public void adaptationsKeepBothDirectionsAtTheDocumentedBounds() {
        StandardGenome seed = StandardGenome.neutral()
                .with(StandardStat.LIGHT_TOLERANCE, -63)
                .with(StandardStat.TEMPERATURE_TOLERANCE, 63);
        assertEquals(-63, seed.get(StandardStat.LIGHT_TOLERANCE));
        assertEquals(63, seed.get(StandardStat.TEMPERATURE_TOLERANCE));
    }

    @Test public void preservingALineCannotBeChangedByEditingAnInputOrDerivedGenome() {
        Map<StandardStat, Integer> source = new EnumMap<>(StandardGenome.neutral().values());
        source.put(StandardStat.YIELD, 8);
        StandardGenome parent = new StandardGenome(source);
        source.put(StandardStat.YIELD, 0);
        StandardGenome candidate = parent.with(StandardStat.YIELD, 5);
        assertEquals(8, parent.get(StandardStat.YIELD));
        assertEquals(5, candidate.get(StandardStat.YIELD));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMagnitudeTemplatesFailInsteadOfSilentlyChangingMeaning() {
        StandardGenome.neutral().with(StandardStat.GROWTH, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void incompleteTemplatesFailInsteadOfInventingMissingGenes() {
        new StandardGenome(new EnumMap<StandardStat, Integer>(StandardStat.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void outOfRangeGenesFailValidation() {
        StandardGenome.neutral().with(StandardStat.RESILIENCE, 64);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void exposedValuesCannotMutateTheSpecimen() {
        StandardGenome.neutral().values().put(StandardStat.GROWTH, 10);
    }
}
