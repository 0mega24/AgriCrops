package dev.omega24.agricrops.api.v1.mutation;

import dev.omega24.agricrops.api.v1.crop.IAgriCrop;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nonnull;

/**
 * Base interface for different cross over strategies.
 */
public interface IAgriCrossStrategy {

    double getRollChance();

    @Nonnull
    Optional<AgriSeed> executeStrategy(@Nonnull IAgriCrop crop, @Nonnull Random rand);

}
