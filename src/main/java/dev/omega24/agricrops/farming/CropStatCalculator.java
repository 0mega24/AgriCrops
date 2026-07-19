package dev.omega24.agricrops.farming;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.reference.AgriCraftConfig;
import java.util.Random;

/**
 * Single home for the growth/harvest/spread math driven by a plant's stat genome
 * (crop-mod-design.md §2: "Keep the aggregation function in one place... so balancing is a
 * numbers change, not a code change.").
 *
 * <p>Only consumes {@link CropStat#GROWTH}, {@link CropStat#YIELD}, and
 * {@link CropStat#RESILIENCE} for now — the remaining stats (water affinity, fertility
 * response, light/temp tolerance, mutation chance) don't have anything to act on yet (soil
 * columns, watering, biome/elevation are later §11 milestones) but are already carried on every
 * genome so plants bred today keep their rolls once those systems land.
 */
public final class CropStatCalculator {

    private CropStatCalculator() {
    }

    /**
     * The chance, per random tick, that a crop advances to its next growth stage.
     */
    public static double growthTickChance(IAgriPlant plant, IAgriStat stat) {
        final double growth = magnitude(stat, CropStat.GROWTH);
        return (plant.getGrowthChanceBase() + growth * plant.getGrowthChanceBonus()) * AgriCraftConfig.growthMultiplier;
    }

    /**
     * How many times to roll the plant's harvest-product table on a mature harvest.
     */
    public static int harvestTrials(IAgriStat stat) {
        return (magnitudeInt(stat, CropStat.YIELD) + 3) / 3;
    }

    /**
     * Whether an aggressive/spreading plant overtakes a neighboring one, weighed by the
     * neighbor's Resilience against the attacker's own.
     */
    public static boolean overtakes(IAgriStat attacker, IAgriStat victim, Random rand) {
        return magnitude(victim, CropStat.RESILIENCE) < magnitude(attacker, CropStat.RESILIENCE) * rand.nextDouble();
    }

    /**
     * Magnitude-only read of a stat: negative rolls (a bad roll on a stat with no "good"
     * negative meaning) contribute nothing rather than working against the formula.
     */
    private static double magnitude(IAgriStat stat, CropStat which) {
        return Math.max(0, stat.get(which));
    }

    private static int magnitudeInt(IAgriStat stat, CropStat which) {
        return Math.max(0, stat.get(which));
    }

}
