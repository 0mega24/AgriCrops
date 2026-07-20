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
 * <p>Consumes {@link CropStat#GROWTH}, {@link CropStat#YIELD}, {@link CropStat#RESILIENCE}
 * (now doing double duty: spread contests, per {@link #overtakes(IAgriStat, IAgriStat,
 * Random)}, and softening the biome/elevation penalty, per {@link #habitatFactor(IAgriStat,
 * double)} since §11 step 4), {@link CropStat#FERTILITY_RESPONSE} (§11 step 2), and {@link
 * CropStat#WATER_AFFINITY} (§11 step 3). Light/Temp Tolerance and Mutation Chance still don't
 * have anything to act on yet but are already carried on every genome so plants bred today
 * keep their rolls once those systems land.
 */
public final class CropStatCalculator {

    /**
     * Below this nutrient-satisfaction floor, growth chance is never fully zeroed out, even
     * with no Fertility Response at all — placeholder balance number, like the rest of this
     * file.
     */
    private static final double MIN_NUTRIENT_FACTOR = 0.5;

    /**
     * Moisture satisfaction of 0.5 (soil column at half its 0..100 range) is baseline —
     * neither a bonus nor a penalty, regardless of Water Affinity. Deviating from it scales
     * the growth chance up or down by up to this fraction at full Water Affinity, per
     * crop-mod-design.md §4: "Water Affinity stat determines how much a given crop's growth
     * chance scales with current moisture vs. baseline." Placeholder balance number.
     */
    private static final double MAX_MOISTURE_SWING = 0.5;
    private static final double MOISTURE_BASELINE = 0.5;

    /**
     * Below this habitat-satisfaction floor, growth chance is never fully zeroed out even for
     * a plant with no Resilience at all, planted entirely outside its preferred biome and
     * elevation band — same shape as {@link #MIN_NUTRIENT_FACTOR}. Placeholder balance number.
     */
    private static final double MIN_HABITAT_FACTOR = 0.4;

    private CropStatCalculator() {
    }

    /**
     * The chance, per random tick, that a crop advances to its next growth stage.
     *
     * @param nutrientSatisfaction the soil column's current nutrients as a fraction of its
     * capacity (0..1), or 1.0 if there's no column to read (e.g. soil not yet touched).
     * @param moistureSatisfaction the soil column's current moisture as a fraction of {@link
     * dev.omega24.agricrops.farming.soil.SoilColumn#MAX_MOISTURE} (0..1), or 0.5 (baseline,
     * no effect) if there's no column to read yet.
     * @param habitatSatisfaction how well the crop's current biome and elevation match the
     * plant's preferences (0..1, 1.0 = fully ideal or the plant has no preference at all), per
     * crop-mod-design.md §5.
     */
    public static double growthTickChance(IAgriPlant plant, IAgriStat stat, double nutrientSatisfaction, double moistureSatisfaction, double habitatSatisfaction) {
        final double growth = magnitude(stat, CropStat.GROWTH);
        final double base = (plant.getGrowthChanceBase() + growth * plant.getGrowthChanceBonus()) * AgriCraftConfig.growthMultiplier;
        return base * nutrientFactor(stat, nutrientSatisfaction) * moistureFactor(stat, moistureSatisfaction) * habitatFactor(stat, habitatSatisfaction);
    }

    /**
     * How much a depleted nutrient pool holds back growth chance, tapering from 1.0 at full
     * nutrients down to a floor (softened by Fertility Response) at zero — same shape as the
     * Resilience-softened penalty in {@link #overtakes(IAgriStat, IAgriStat, Random)}.
     */
    private static double nutrientFactor(IAgriStat stat, double nutrientSatisfaction) {
        final double fertilityResponse = magnitude(stat, CropStat.FERTILITY_RESPONSE) / CropStat.MAX;
        final double floor = MIN_NUTRIENT_FACTOR + (1.0 - MIN_NUTRIENT_FACTOR) * fertilityResponse;
        return floor + (1.0 - floor) * Math.min(1.0, Math.max(0.0, nutrientSatisfaction));
    }

    /**
     * How much moisture (relative to the {@link #MOISTURE_BASELINE}) helps or hurts growth
     * chance, scaled by Water Affinity: a plant with 0 Water Affinity is a moisture
     * generalist unaffected either way, while a high-Water-Affinity plant swings hard toward
     * a bonus when moist and a penalty when dry.
     */
    private static double moistureFactor(IAgriStat stat, double moistureSatisfaction) {
        final double waterAffinity = magnitude(stat, CropStat.WATER_AFFINITY) / CropStat.MAX;
        final double deviation = Math.min(1.0, Math.max(0.0, moistureSatisfaction)) - MOISTURE_BASELINE;
        return 1.0 + deviation * 2.0 * MAX_MOISTURE_SWING * waterAffinity;
    }

    /**
     * How much being outside the plant's preferred biome/elevation holds back growth chance,
     * tapering from 1.0 at full habitat satisfaction down to a floor (softened by Resilience)
     * at zero — same shape as {@link #nutrientFactor(IAgriStat, double)}, using Resilience
     * instead of Fertility Response since crop-mod-design.md §5 calls out both the biome and
     * elevation penalties as "softened by Resilience".
     */
    private static double habitatFactor(IAgriStat stat, double habitatSatisfaction) {
        final double resilience = magnitude(stat, CropStat.RESILIENCE) / CropStat.MAX;
        final double floor = MIN_HABITAT_FACTOR + (1.0 - MIN_HABITAT_FACTOR) * resilience;
        return floor + (1.0 - floor) * Math.min(1.0, Math.max(0.0, habitatSatisfaction));
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
