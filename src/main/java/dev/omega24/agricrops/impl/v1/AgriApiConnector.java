/*
 */
package dev.omega24.agricrops.impl.v1;

import dev.omega24.agricrops.api.v1.AgriApiState;
import dev.omega24.agricrops.api.v1.IAgriApiConnector;
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
public class AgriApiConnector implements IAgriApiConnector {

    @Nonnull
    private final IAgriRegistry<IAgriPlant> plantRegistry;
    @Nonnull
    private final IAgriMutationRegistry mutationRegistry;
    @Nonnull
    private final IAgriSoilRegistry soilRegistry;
    @Nonnull
    private final IAgriAdapterizer<IAgriStat> statRegistry;
    @Nonnull
    private final IAgriAdapterizer<IAgriStatCalculator> statCalculatorRegistry;
    @Nonnull
    private final IAgriMutationEngine mutationEngine;
    @Nonnull
    private final IAgriAdapterizer<AgriSeed> seedRegistry;
    @Nonnull
    private final IAgriAdapterizer<IAgriFertilizer> fertilizerRegistry;
    @Nonnull
    private final IAgriRegistry<IAgriPeripheralMethod> peripheralMethodRegistry;

    public AgriApiConnector() {
        this.plantRegistry = new AgriRegistry<>();
        this.mutationRegistry = new AgriMutationRegistry();
        this.soilRegistry = new AgriSoilRegistry();
        this.statRegistry = new AgriAdapterizer<>();
        this.statCalculatorRegistry = new AgriAdapterizer<>();
        this.mutationEngine = new AgriMutationEngine();
        this.seedRegistry = new AgriAdapterizer<>();
        this.fertilizerRegistry = new AgriAdapterizer<>();
        this.peripheralMethodRegistry = new AgriRegistry<>();
    }

    @Override
    @Nonnull
    public AgriApiState getState() {
        return AgriApiState.VALID;
    }

    @Override
    @Nonnull
    public IAgriRegistry<IAgriPlant> connectPlantRegistry() {
        return this.plantRegistry;
    }

    @Override
    @Nonnull
    public IAgriMutationRegistry connectMutationRegistry() {
        return this.mutationRegistry;
    }

    @Override
    @Nonnull
    public IAgriSoilRegistry connectSoilRegistry() {
        return this.soilRegistry;
    }

    @Override
    @Nonnull
    public IAgriAdapterizer<IAgriStat> connectStatRegistry() {
        return this.statRegistry;
    }

    @Override
    @Nonnull
    public IAgriAdapterizer<IAgriStatCalculator> connectStatCalculatorRegistry() {
        return this.statCalculatorRegistry;
    }

    @Override
    @Nonnull
    public IAgriMutationEngine connectMutationEngine() {
        return this.mutationEngine;
    }

    @Override
    @Nonnull
    public IAgriAdapterizer<AgriSeed> connectSeedRegistry() {
        return this.seedRegistry;
    }

    @Override
    @Nonnull
    public IAgriAdapterizer<IAgriFertilizer> connectFertilizerRegistry() {
        return this.fertilizerRegistry;
    }

    @Override
    @Nonnull
    public IAgriRegistry<IAgriPeripheralMethod> connectPeripheralMethodRegistry() {
        return this.peripheralMethodRegistry;
    }

}
