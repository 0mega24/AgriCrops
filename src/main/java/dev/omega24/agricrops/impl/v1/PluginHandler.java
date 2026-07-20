/*
 */
package dev.omega24.agricrops.impl.v1;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.plugin.AgriPlugin;
import dev.omega24.agricrops.api.v1.plugin.IAgriPlugin;
import dev.omega24.agricrops.api.v1.adapter.IAgriAdapterizer;
import dev.omega24.agricrops.api.v1.fertilizer.IAgriFertilizer;
import dev.omega24.agricrops.api.v1.misc.IAgriPeripheralMethod;
import dev.omega24.agricrops.api.v1.misc.IAgriRegistry;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutationEngine;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutationRegistry;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import dev.omega24.agricrops.api.v1.soil.ISoilProfileRegistry;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStatCalculator;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 *
 *
 */
public final class PluginHandler {

    @Nonnull
    private static final Deque<IAgriPlugin> PLUGINS = new ConcurrentLinkedDeque<>();

    public static void preInit(FMLPreInitializationEvent event) {
        PLUGINS.addAll(getInstances(event.getAsmData(), AgriPlugin.class, IAgriPlugin.class));
        PLUGINS.stream()
                .peek(PluginHandler::logPlugin)
                .filter(IAgriPlugin::isEnabled)
                .forEach(MinecraftForge.EVENT_BUS::register);
    }

    public static void init() {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach(IAgriPlugin::initPlugin);
    }

    public static void postInit() {
        registerSoilCompositions(AgriApi.getSoilCompositionRegistry());
        registerSoilProfiles(AgriApi.getSoilProfileRegistry());
        registerPlants(AgriApi.getPlantRegistry());
        registerMutations(AgriApi.getMutationRegistry());
        registerStats(AgriApi.getStatRegistry());
        registerSeeds(AgriApi.getSeedRegistry());
        registerFertilizers(AgriApi.getFertilizerRegistry());
        registerStatCalculators(AgriApi.getStatCalculatorRegistry());
        registerCrossStrategies(AgriApi.getMutationEngine());
        registerPeripheralMethods(AgriApi.getPeripheralMethodRegistry());
    }

    public static void loadTextures(Consumer<ResourceLocation> registry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerTextures(registry));
    }

    public static void registerSoilCompositions(IAgriRegistry<ISoilComposition> soilCompositionRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerSoilCompositions(soilCompositionRegistry));
    }

    public static void registerSoilProfiles(ISoilProfileRegistry soilProfileRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerSoilProfiles(soilProfileRegistry));
    }

    public static void registerPlants(IAgriRegistry<IAgriPlant> plantRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerPlants(plantRegistry));
    }

    public static void registerMutations(IAgriMutationRegistry mutationRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerMutations(mutationRegistry));
    }

    public static void registerStats(IAgriAdapterizer<IAgriStat> statRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerStats(statRegistry));
    }

    public static void registerSeeds(IAgriAdapterizer<AgriSeed> seedRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerSeeds(seedRegistry));
    }

    public static void registerFertilizers(IAgriAdapterizer<IAgriFertilizer> fertilizerRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerFertilizers(fertilizerRegistry));
    }

    public static void registerStatCalculators(IAgriAdapterizer<IAgriStatCalculator> calculatorRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach((p) -> p.registerStatCalculators(calculatorRegistry));
    }

    public static void registerCrossStrategies(IAgriMutationEngine mutationEngine) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach(p -> p.registerCrossStrategies(mutationEngine));
    }
    
    public static void registerPeripheralMethods(IAgriRegistry<IAgriPeripheralMethod> methodRegistry) {
        PLUGINS.stream().filter(IAgriPlugin::isEnabled).forEach(p -> p.registerPeripheralMethods(methodRegistry));
    }

    /**
     * Loads classes with a specific annotation from an asm data table.
     *
     * Borrowed from JEI's source code, which is licensed under the MIT license.
     *
     * @param <T> The type of class to load.
     * @param asm The asm data table to load classes from.
     * @param anno The annotation marking classes of interest.
     * @param type The class type to load, as to get around Type erasure.
     * @return A list of the loaded classes, instantiated.
     */
    @Nonnull
    private static <T> List<T> getInstances(ASMDataTable asm, Class anno, Class<T> type) {
        final List<T> instances = new ArrayList<>();
        for (ASMDataTable.ASMData asmData : asm.getAll(anno.getCanonicalName())) {
            try {
                T instance = Class.forName(asmData.getClassName()).asSubclass(type).newInstance();
                instances.add(instance);
            } catch (ClassNotFoundException | NoClassDefFoundError | IllegalAccessException | InstantiationException e) {
                AgriCore.getLogger("agricrops-plugins").debug(
                        "%nFailed to load AgriPlugin%n\tOf class: {0}!%n\tFor annotation: {1}!%n\tAs Instanceof: {2}!",
                        asmData.getClassName(),
                        anno.getCanonicalName(),
                        type.getCanonicalName()
                );
            }
        }
        return instances;
    }
    
    private static void logPlugin(IAgriPlugin plugin) {
        AgriCore.getLogger("agricrops").info("\nFound AgriCraft Plugin:\n\t- Id: {0}\n\t- Name: {1}\n\t- Status: {2}", plugin.getId(), plugin.getName(), plugin.isEnabled() ? "Enabled" : "Disabled");
    }

}
