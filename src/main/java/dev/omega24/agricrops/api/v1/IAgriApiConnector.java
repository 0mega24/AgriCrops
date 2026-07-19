/*
 */
package dev.omega24.agricrops.api.v1;

import dev.omega24.agricrops.api.v1.adapter.IAgriAdapterizer;
import dev.omega24.agricrops.api.v1.fertilizer.IAgriFertilizer;
import dev.omega24.agricrops.api.v1.misc.IAgriPeripheralMethod;
import dev.omega24.agricrops.api.v1.misc.IAgriRegistry;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutationEngine;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutationRegistry;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.soil.IAgriSoilRegistry;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStatCalculator;
import javax.annotation.Nonnull;

/**
 *
 * @author Ryan
 */
public interface IAgriApiConnector {

    IAgriApiConnector FAKE = new AgriApiConnectorFake();

    @Nonnull
    AgriApiState getState();

    @Nonnull
    IAgriRegistry<IAgriPlant> connectPlantRegistry();

    @Nonnull
    IAgriMutationRegistry connectMutationRegistry();

    @Nonnull
    IAgriSoilRegistry connectSoilRegistry();

    @Nonnull
    IAgriAdapterizer<IAgriStat> connectStatRegistry();

    @Nonnull
    IAgriAdapterizer<IAgriStatCalculator> connectStatCalculatorRegistry();

    @Nonnull
    IAgriMutationEngine connectMutationEngine();

    @Nonnull
    IAgriAdapterizer<AgriSeed> connectSeedRegistry();

    @Nonnull
    IAgriAdapterizer<IAgriFertilizer> connectFertilizerRegistry();

    @Nonnull
    IAgriRegistry<IAgriPeripheralMethod> connectPeripheralMethodRegistry();

}
