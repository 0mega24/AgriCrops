package dev.omega24.agricrops.farming;

import dev.omega24.agricrops.api.v1.species.CropSpecies;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import java.util.Optional;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenomeFactoryTest {

    @Test
    public void freshStatsUseTheRegisteredSpeciesTemplate() {
        CropSpecies species = CropSpecies.builder("test_crop")
                .baseStat(CropStat.GROWTH, 12)
                .baseStat(CropStat.YIELD, -4)
                .baseStat(CropStat.LIGHT_TOLERANCE, -8)
                .build();
        GenomeFactory factory = new GenomeFactory(id ->
                "test_crop".equals(id) ? Optional.of(species) : Optional.empty());

        IAgriStat stats = factory.createFreshStats("test_crop", false);

        assertEquals(12, stats.get(CropStat.GROWTH));
        assertEquals(-4, stats.get(CropStat.YIELD));
        assertEquals(-8, stats.get(CropStat.LIGHT_TOLERANCE));
        assertEquals(0, stats.get(CropStat.TEMP_TOLERANCE));
        assertFalse(stats.isAnalyzed());
    }

    @Test
    public void missingSpeciesFallsBackToSafeWildType() {
        GenomeFactory factory = new GenomeFactory(id -> Optional.empty());

        IAgriStat stats = factory.createFreshStats("legacy_crop", true);

        for (CropStat stat : CropStat.values()) {
            assertEquals(0, stats.get(stat));
        }
        assertTrue(stats.isAnalyzed());
    }
}
