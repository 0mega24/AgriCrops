package dev.omega24.agricrops.api.v1.stat;

import dev.omega24.agricrops.api.v1.crop.IAgriCrop;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutation;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import java.util.Collection;
import java.util.Random;
import javax.annotation.Nonnull;

/**
 * Interface to create custom Stat Calculator logic use API.setStatCalculator(IStatCalculator
 * calculator) to set the active calculator
 */
public interface IAgriStatCalculator {

    /**
     * Calculates the stats for a spread event.
     *
     * @param child the child that was the result of the spreading.
     * @param input a List containing all neighboring crops
     * @return an ISeedStats object containing the resulting stats
     */
    @Nonnull
    IAgriStat calculateSpreadStats(@Nonnull IAgriPlant child, @Nonnull Collection<IAgriCrop> input);

    /**
     * Calculates the stats for a mutation.
     *
     * @param mutation The mutation that occurred.
     * @param input a List containing all neighboring crops
     * @param rand the random source to roll locked-attribute unlocks with (crop-mod-design.md §7)
     * @return an ISeedStats object containing the resulting stats
     */
    @Nonnull
    IAgriStat calculateMutationStats(@Nonnull IAgriMutation mutation, @Nonnull Collection<IAgriCrop> input, @Nonnull Random rand);

}
