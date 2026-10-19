package dev.omega24.agricrops.impl.v1;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.attribute.AttributeAllele;
import dev.omega24.agricrops.api.v1.attribute.LockedAttribute;
import dev.omega24.agricrops.api.v1.attribute.LockedAttributeInstance;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.farming.PlantStats;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttributeUnlockRegistryTest {

    @Test
    public void unstableSingleParentTraitCanDropOut() {
        LockedAttribute definition = definition("test_dropout", 3);
        IAgriStat carrier = genome(instance(definition, 0));

        List<LockedAttributeInstance> result = new AttributeUnlockRegistry().rollCross(
                "a", carrier, "b", new PlantStats(), new FixedRandom(0.0));

        assertTrue(result.isEmpty());
    }

    @Test
    public void stabilizedSingleParentTraitDoesNotDropOut() {
        LockedAttribute definition = definition("test_stable", 2);
        IAgriStat carrier = genome(instance(definition, 2));

        List<LockedAttributeInstance> result = new AttributeUnlockRegistry().rollCross(
                "a", carrier, "b", new PlantStats(), new FixedRandom(0.0));

        assertEquals(1, result.size());
        assertEquals(definition.getId(), result.get(0).getAttributeId());
    }

    @Test
    public void twoCarrierParentsAdvanceBredTrueGeneration() {
        LockedAttribute definition = definition("test_bred_true", 3);
        IAgriStat first = genome(instance(definition, 1));
        IAgriStat second = genome(instance(definition, 2));

        List<LockedAttributeInstance> result = new AttributeUnlockRegistry().rollCross(
                "a", first, "b", second, new FixedRandom(1.0));

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getGenerationsBredTrue());
    }

    private static LockedAttribute definition(String id, int threshold) {
        LockedAttribute definition = new LockedAttribute(id, false, threshold);
        AgriApi.getAttributeRegistry().add(definition);
        return definition;
    }

    private static LockedAttributeInstance instance(LockedAttribute definition, int generations) {
        return new LockedAttributeInstance(definition.getId(),
                new AttributeAllele(8, true), new AttributeAllele(5, false), generations);
    }

    private static IAgriStat genome(LockedAttributeInstance instance) {
        return new PlantStats().withLockedAttributes(Collections.singletonList(instance));
    }

    private static final class FixedRandom extends Random {
        private final double value;

        private FixedRandom(double value) {
            this.value = value;
        }

        @Override
        public double nextDouble() {
            return this.value;
        }

        @Override
        public boolean nextBoolean() {
            return true;
        }
    }
}
