/*
 */
package dev.omega24.agricrops.api.v1;

import dev.omega24.agricrops.api.v1.adapter.IAgriAdapterizer;
import dev.omega24.agricrops.api.v1.attribute.IAttributeUnlockRegistry;
import dev.omega24.agricrops.api.v1.attribute.LockedAttribute;
import dev.omega24.agricrops.api.v1.fertilizer.IAgriFertilizer;
import dev.omega24.agricrops.api.v1.misc.IAgriPeripheralMethod;
import dev.omega24.agricrops.api.v1.misc.IAgriRegistry;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutationEngine;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutationRegistry;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import dev.omega24.agricrops.api.v1.soil.ISoilProfileRegistry;
import dev.omega24.agricrops.api.v1.species.CropSpecies;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStatCalculator;
import javax.annotation.Nonnull;

/**
 * A fake API connector, for when the API was not found.
 */
final class AgriApiConnectorFake implements IAgriApiConnector {

    @Override
    @Nonnull
    public AgriApiState getState() {
        return AgriApiState.INVALID;
    }

    @Override
    @Nonnull
    public IAgriRegistry<IAgriPlant> connectPlantRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriMutationRegistry connectMutationRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public ISoilProfileRegistry connectSoilProfileRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriRegistry<ISoilComposition> connectSoilCompositionRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriAdapterizer<IAgriStat> connectStatRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriAdapterizer<IAgriStatCalculator> connectStatCalculatorRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriMutationEngine connectMutationEngine() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriAdapterizer<AgriSeed> connectSeedRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriAdapterizer<IAgriFertilizer> connectFertilizerRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    public IAgriRegistry<IAgriPeripheralMethod> connectPeripheralMethodRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriRegistry<CropSpecies> connectSpeciesRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAgriRegistry<LockedAttribute> connectAttributeRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

    @Override
    @Nonnull
    public IAttributeUnlockRegistry connectAttributeUnlockRegistry() {
        throw new UnsupportedOperationException("The stand-in version of the AgriCraft API does not support this operation.");
    }

}
