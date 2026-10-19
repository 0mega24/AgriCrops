package dev.omega24.agricrops.farming.mutation.statcalculator;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.adapter.IAgriAdapter;
import dev.omega24.agricrops.api.v1.attribute.LockedAttributeInstance;
import dev.omega24.agricrops.api.v1.species.CropSpecies;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class StatCalculatorBase implements IAgriStatCalculator, IAgriAdapter<IAgriStatCalculator> {

    @Override
    public IAgriStat calculateSpreadStats(IAgriPlant child, Collection<IAgriCrop> parents, Random rand) {
        Objects.requireNonNull(child, "The child plant to calculate the stats for must not be null!");
        Objects.requireNonNull(parents, "The set of parents to calculate the child's stats from must not be null!");
        Objects.requireNonNull(rand, "The random source to calculate spread stats must not be null!");
        return blend(parents, parentPlant -> Objects.equals(child, parentPlant), 1, rand);
    }

    @Override
    public IAgriStat calculateMutationStats(IAgriMutation mutation, Collection<IAgriCrop> parents, Random rand) {
        Objects.requireNonNull(mutation, "The mutation to calculate the stats for must not be null!");
        Objects.requireNonNull(parents, "The set of parents to calculate the mutation result stats from must not be null!");
        Objects.requireNonNull(rand, "The random source to roll locked-attribute unlocks with must not be null!");
        IAgriStat result = blend(parents, mutation::hasParent, AgriCraftConfig.cropStatDivisor, rand);
        return rollLockedAttributes(mutation, parents, result, rand);
    }

    /**
     * Species pairing for locked-attribute unlocks (crop-mod-design.md §7) only applies to
     * mutations with exactly two required parent species — the registry itself is
     * pair-keyed, so a 3+ parent mutation has no single pair to roll against and is left
     * with whatever attributes the stat blend already carries (none, since {@code blend}
     * never touches them).
     */
    private IAgriStat rollLockedAttributes(IAgriMutation mutation, Collection<IAgriCrop> parents, IAgriStat result, Random rand) {
        List<IAgriPlant> required = mutation.getParents();
        if (required.size() != 2) {
            return result;
        }
        IAgriPlant plantA = required.get(0);
        IAgriPlant plantB = required.get(1);
        IAgriStat statA = findGenomeOf(plantA, parents);
        IAgriStat statB = findGenomeOf(plantB, parents);
        List<LockedAttributeInstance> unlocked = AgriApi.getAttributeUnlockRegistry()
                .rollCross(plantA.getId(), statA, plantB.getId(), statB, rand);
        // An unlock rule cannot conjure storage on a child species that has no slot for it.
        // Missing species definitions therefore fail closed instead of leaking traits.
        final java.util.Optional<CropSpecies> childSpecies =
                AgriApi.getSpeciesRegistry().get(mutation.getChild().getId());
        if (!childSpecies.isPresent()) {
            return result;
        }
        unlocked = unlocked.stream()
                .filter(instance -> AgriApi.getAttributeRegistry().get(instance.getAttributeId())
                        .map(childSpecies.get()::canUnlock)
                        .orElse(false))
                .collect(Collectors.toList());
        return result.withLockedAttributes(unlocked);
    }

    private static IAgriStat findGenomeOf(IAgriPlant plant, Collection<IAgriCrop> parents) {
        for (IAgriCrop parent : parents) {
            if (parent == null) {
                continue;
            }
            final AgriSeed parentSeed = parent.getSeed();
            if (parentSeed != null && Objects.equals(parentSeed.getPlant(), plant)) {
                return parentSeed.getStat();
            }
        }
        return null;
    }

    /**
     * Sums every stat across valid parents, averages, then hands each average through
     * {@link #calculateStat(int, int, int)} independently — same shape as the old
     * growth/gain/strength blend, just generalized over the full {@link CropStat} set.
     */
    private IAgriStat blend(Collection<IAgriCrop> parents, Predicate<IAgriPlant> isValidParentPlant,
                            int divisor, Random rand) {
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
            result = result.with(stat, calculateStat(mean, validParents, divisor, rand));
        }
        return result;
    }

    /**
     * calculates the new stats based on an input stat, the nr of neighbours and a divisor
     */
    protected abstract int calculateStat(int input, int neighbours, int divisor, Random rand);

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
