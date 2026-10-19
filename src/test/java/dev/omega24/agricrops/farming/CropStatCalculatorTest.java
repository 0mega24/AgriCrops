package dev.omega24.agricrops.farming;

import dev.omega24.agricrops.api.v1.attribute.AttributeAllele;
import dev.omega24.agricrops.api.v1.attribute.LockedAttributeInstance;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.core.DefaultSpecies;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CropStatCalculatorTest {

    @Test
    public void lightAdaptationPrefersItsOwnDirection() {
        IAgriStat bright = new PlantStats().with(CropStat.LIGHT_TOLERANCE, CropStat.MAX);
        IAgriStat dark = new PlantStats().with(CropStat.LIGHT_TOLERANCE, CropStat.MIN);

        assertTrue(CropStatCalculator.environmentFactor(bright, 15, 0.8)
                > CropStatCalculator.environmentFactor(dark, 15, 0.8));
        assertTrue(CropStatCalculator.environmentFactor(dark, 8, 0.8)
                > CropStatCalculator.environmentFactor(bright, 8, 0.8));
    }

    @Test
    public void frostHardinessImprovesColdGrowthButNotWarmGrowth() {
        IAgriStat ordinary = new PlantStats();
        LockedAttributeInstance frost = new LockedAttributeInstance(
                DefaultSpecies.FROST_HARDINESS.getId(),
                new AttributeAllele(CropStat.MAX, true),
                new AttributeAllele(CropStat.MAX, true), 3);
        IAgriStat hardy = ordinary.withLockedAttributes(Collections.singletonList(frost));

        assertTrue(CropStatCalculator.environmentFactor(hardy, 12, -0.5)
                > CropStatCalculator.environmentFactor(ordinary, 12, -0.5));
        assertTrue(Math.abs(CropStatCalculator.environmentFactor(hardy, 12, 1.5)
                - CropStatCalculator.environmentFactor(ordinary, 12, 1.5)) < 0.000001);
    }
}
