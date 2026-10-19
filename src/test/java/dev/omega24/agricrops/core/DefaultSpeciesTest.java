package dev.omega24.agricrops.core;

import dev.omega24.agricrops.api.v1.species.CropSpecies;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultSpeciesTest {

    @Test
    public void builtInFreshSeedsHaveModestNonzeroMagnitudeTemplates() {
        CropSpecies species = DefaultSpecies.createPlainSpecies("carrot");

        assertEquals(1, species.getBaseStat(CropStat.GROWTH));
        assertEquals(1, species.getBaseStat(CropStat.YIELD));
        assertEquals(1, species.getBaseStat(CropStat.RESILIENCE));
        assertEquals(1, species.getBaseStat(CropStat.FERTILITY_RESPONSE));
        assertEquals(0, species.getBaseStat(CropStat.WATER_AFFINITY));
        assertEquals(0, species.getBaseStat(CropStat.LIGHT_TOLERANCE));
        assertEquals(0, species.getBaseStat(CropStat.TEMP_TOLERANCE));
        assertEquals(0, species.getBaseStat(CropStat.MUTATION_CHANCE));
    }

    @Test
    public void winterWheatTemplateMatchesItsColdWeatherRole() {
        CropSpecies species = DefaultSpecies.createPlainSpecies("winter_wheat");

        assertEquals(3, species.getBaseStat(CropStat.RESILIENCE));
        assertEquals(-8, species.getBaseStat(CropStat.TEMP_TOLERANCE));
        assertTrue(species.canUnlock(DefaultSpecies.FROST_HARDINESS));
    }
}
