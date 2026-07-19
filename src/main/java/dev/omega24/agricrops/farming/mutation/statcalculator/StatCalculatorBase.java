package dev.omega24.agricrops.farming.mutation.statcalculator;

import dev.omega24.agricrops.api.v1.adapter.IAgriAdapter;
import dev.omega24.agricrops.api.v1.crop.IAgriCrop;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutation;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStatCalculator;
import dev.omega24.agricrops.farming.PlantStats;
import dev.omega24.agricrops.reference.AgriCraftConfig;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class StatCalculatorBase implements IAgriStatCalculator, IAgriAdapter<IAgriStatCalculator> {

    @Override
    public IAgriStat calculateSpreadStats(IAgriPlant child, Collection<IAgriCrop> parents) {
        Objects.requireNonNull(child, "The child plant to calculate the stats for must not be null!");
        Objects.requireNonNull(parents, "The set of parents to calculate the child's stats from must not be null!");
        return blend(parents, parentPlant -> Objects.equals(child, parentPlant), 1);
    }

    @Override
    public IAgriStat calculateMutationStats(IAgriMutation mutation, Collection<IAgriCrop> parents) {
        Objects.requireNonNull(mutation, "The mutation to calculate the stats for must not be null!");
        Objects.requireNonNull(parents, "The set of parents to calculate the mutation result stats from must not be null!");
        return blend(parents, mutation::hasParent, AgriCraftConfig.cropStatDivisor);
    }

    /**
     * Sums every stat across valid parents, averages, then hands each average through
     * {@link #calculateStat(int, int, int)} independently — same shape as the old
     * growth/gain/strength blend, just generalized over the full {@link CropStat} set.
     */
    private IAgriStat blend(Collection<IAgriCrop> parents, Predicate<IAgriPlant> isValidParentPlant, int divisor) {
        int invalidParents = 0;
        int validParents = 0;
        final Map<CropStat, Integer> sums = new EnumMap<>(CropStat.class);
        for (CropStat stat : CropStat.values()) {
            sums.put(stat, 0);
        }

        for (IAgriCrop parent : parents) {
            if (parent == null) {
                continue;
            }
            final AgriSeed parentSeed = parent.getSeed();
            if (parentSeed == null) {
                continue;
            }
            if (!parent.isMature()) {
                invalidParents++;
                continue;
            }
            if (!isValidParentPlant.test(parentSeed.getPlant())) {
                invalidParents++;
                continue;
            }
            validParents++;
            for (CropStat stat : CropStat.values()) {
                sums.merge(stat, (int) parentSeed.getStat().get(stat), Integer::sum);
            }
        }

        final int meanDivisor = calculateStatMeanDivisor(validParents, invalidParents);

        IAgriStat result = new PlantStats();
        for (CropStat stat : CropStat.values()) {
            final int mean = sums.get(stat) / meanDivisor;
            result = result.with(stat, calculateStat(mean, validParents, divisor));
        }
        return result;
    }

    /**
     * calculates the new stats based on an input stat, the nr of neighbours and a divisor
     */
    protected abstract int calculateStat(int input, int neighbours, int divisor);

    /**
     * Calculates the divisor to use for the mean operation on the stats of the parent plants.
     *
     * @param validParents The total number of valid parents.
     * @param invalidParents The total number of invalid parents.
     * @return The divisor to use for averaging the stats of the parent plants.
     */
    public static final int calculateStatMeanDivisor(int validParents, int invalidParents) {
        // Allocate the divisor.
        int meanDivisor = validParents;

        // Account for invalid parents, if enabled in config.
        if (AgriCraftConfig.otherCropsAffectStatsNegatively) {
            meanDivisor = meanDivisor + invalidParents;
        }

        // Verify that the mean divisor is at least one.
        if (meanDivisor < 1) {
            meanDivisor = 1;
        }

        // Return the calculated divisor.
        return meanDivisor;
    }

}
