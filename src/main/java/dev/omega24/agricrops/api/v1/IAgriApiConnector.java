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
 *
 * @author Ryan
 */
public interface IAgriApiConnector {

    IAgriApiConnector FAKE = new AgriApiConnectorFake();

    @Nonnull
    AgriApiState getState();

    @Nonnull
    IAgriRegistry<IAgriPlant> connectPlantRegistry();

    /**
     * The species registry (crop-mod-design.md §7): base stat templates + locked-attribute
     * slots, one {@link CropSpecies} per {@link IAgriPlant} id.
     */
    @Nonnull
    IAgriRegistry<CropSpecies> connectSpeciesRegistry();

    /**
     * The open-population locked-attribute registry (crop-mod-design.md §7): extra genome slots
     * that don't exist on a species until a cross-species unlock rule activates them.
     */
    @Nonnull
    IAgriRegistry<LockedAttribute> connectAttributeRegistry();

    /**
     * Species-pair unlock rules and hard-incompatibility flags (crop-mod-design.md §7).
     */
    @Nonnull
    IAttributeUnlockRegistry connectAttributeUnlockRegistry();

    @Nonnull
    IAgriMutationRegistry connectMutationRegistry();

    @Nonnull
    ISoilProfileRegistry connectSoilProfileRegistry();

    @Nonnull
    IAgriRegistry<ISoilComposition> connectSoilCompositionRegistry();

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
