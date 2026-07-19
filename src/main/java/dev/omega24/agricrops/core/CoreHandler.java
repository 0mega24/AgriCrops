package dev.omega24.agricrops.core;

import com.agricraft.agricore.config.AgriConfigAdapter;
import com.agricraft.agricore.core.AgriCore;
import com.agricraft.agricore.json.AgriLoader;
import com.agricraft.agricore.plant.AgriMutation;
import com.agricraft.agricore.plant.AgriPlant;
import com.agricraft.agricore.plant.AgriSoil;
import com.agricraft.agricore.util.ResourceHelper;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.mutation.IAgriMutation;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.soil.IAgriSoil;
import dev.omega24.agricrops.reference.Reference;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class CoreHandler {

    public static final Pattern JSON_FILE_PATTERN = Pattern.compile(".*\\.json", Pattern.CASE_INSENSITIVE);
    public static final Pattern AGRI_FOLDER_PATTERN = Pattern.compile("json/defaults/.*", Pattern.CASE_INSENSITIVE);

    private static Path configDir;
    private static Path jsonDir;
    private static Path defaultDir;
    private static Configuration config;

    private CoreHandler() {
    }

    public static Configuration getConfig() {
        return config;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static Path getJsonDir() {
        return jsonDir;
    }

    public static void preInit(FMLPreInitializationEvent event) {
        // Setup Config.
        configDir = event.getSuggestedConfigurationFile().getParentFile().toPath().resolve(Reference.MOD_ID);
        config = new Configuration(configDir.resolve("config.cfg").toFile());

        // Setup Plant Dir.
        jsonDir = configDir.resolve("json");
        defaultDir = jsonDir.resolve("defaults");

        // Setup Provider
        AgriConfigAdapter provider = new ModProvider(config);
        MinecraftForge.EVENT_BUS.register(provider);

        // Initialize AgriCore
        AgriCore.init(new ModLogger(), new ModTranslator(), new ModValidator(), new ModConverter(), provider);

        // Transfer Defaults
        ResourceHelper.findResources(JSON_FILE_PATTERN.asPredicate()).stream()
                .filter(AGRI_FOLDER_PATTERN.asPredicate())
                .forEach(r -> ResourceHelper.copyResource(r, configDir.resolve(r), false)
                );

        // Load the JSON files.
        loadJsons();

        // Fork-native baseline soils/plants in code, registered here (not in init()) so
        // their textures are present in AgriApi's registries before the block-atlas
        // TextureStitchEvent fires (that event fires before FMLInitializationEvent).
        // Soils first: plants look up their growth-requirement soils by id at construction.
        DefaultSoils.register();
        DefaultPlants.register();
    }

    public static void init() {
        // Save settings!
        AgriCore.getConfig().save();

        // Load JSON Stuff
        initSoils();
        initPlants();
        initMutations();
    }

    public static void loadJsons() {
        // Load the core!
        AgriCore.getLogger("agricrops").info("Attempting to read AgriCraft JSONs!");
        AgriLoader.loadDirectory(
                defaultDir,
                AgriCore.getSoils(),
                AgriCore.getPlants(),
                AgriCore.getMutations()
        );
        AgriCore.getLogger("agricrops").info("Finished trying to read AgriCraft JSONs!");
    }

    public static void initSoils() {
        // Announce Progress
        AgriCore.getLogger("agricrops").info("Registering Soils!");

        // See if soils are valid...
        final int raw = AgriCore.getSoils().getAll().size();
        AgriCore.getSoils().validate();
        final int count = AgriCore.getSoils().getAll().size();

        // Transfer
        AgriCore.getSoils().getAll().stream()
                .filter(AgriSoil::isEnabled)
                .map(JsonSoil::new)
                .forEach(AgriApi.getSoilRegistry()::add);

        // Fork-native base soils in code (until the data-driven soil registry, design §3).
        // Registered earlier in preInit(); see the comment there for why.

        // Display Soils
        AgriCore.getLogger("agricrops").info("Registered Soils ({0} total; {1}/{2} valid from JSON):",
                AgriApi.getSoilRegistry().all().size(), count, raw);
        for (IAgriSoil soil : AgriApi.getSoilRegistry().all()) {
            AgriCore.getLogger("agricrops").info(" - {0}", soil.getName());
        }
    }

    public static void initPlants() {
        // Announce Progress
        AgriCore.getLogger("agricrops").info("Registering Plants!");

        // See if plants are valid...
        final int raw = AgriCore.getPlants().getAll().size();
        AgriCore.getPlants().validate();
        final int count = AgriCore.getPlants().getAll().size();

        // Transfer
        AgriCore.getPlants().validate();
        AgriCore.getPlants().getAll().stream()
                .filter(AgriPlant::isEnabled)
                .map(JsonPlant::new)
                .forEach(AgriApi.getPlantRegistry()::add);

        // Fork-native baseline plants in code (until the species/attribute registries, design §7).
        // Registered earlier in preInit(); see the comment there for why.

        // Display Plants
        AgriCore.getLogger("agricrops").info("Registered Plants ({0}/{1}):", count, raw);
        for (IAgriPlant plant : AgriApi.getPlantRegistry().all()) {
            AgriCore.getLogger("agricrops").info(" - {0}", plant.getPlantName());
        }
    }

    public static void initMutations() {
        // Announce Progress
        AgriCore.getLogger("agricrops").info("Registering Mutations!");

        // See if mutations are valid...
        final int raw = AgriCore.getMutations().getAll().size();
        AgriCore.getMutations().validate();
        final int count = AgriCore.getMutations().getAll().size();

        // Transfer
        AgriCore.getMutations().getAll().stream()
                .filter(AgriMutation::isEnabled)
                .map(JsonHelper::wrap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(AgriApi.getMutationRegistry()::add);

        // Display Mutations
        AgriCore.getLogger("agricrops").info("Registered Mutations ({0}/{1}):", count, raw);
        for (IAgriMutation mutation : AgriApi.getMutationRegistry().all()) {
            AgriCore.getLogger("agricrops").info(" - {0}", mutation);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void loadTextures(Consumer<ResourceLocation> consumer) {
        AgriCore.getPlants().getAll().stream()
                .flatMap(plant -> plant.getTexture().getAllTextures().stream())
                .distinct()
                .map(t -> new ResourceLocation(t))
                .forEach(consumer);

        // Code-registered plants (DefaultPlants) aren't backed by AgriCore JSON, so their
        // growth-stage/seed textures need stitching here too.
        for (IAgriPlant plant : AgriApi.getPlantRegistry().all()) {
            for (int stage = 0; stage < plant.getGrowthStages(); stage++) {
                ResourceLocation primary = plant.getPrimaryPlantTexture(stage);
                if (primary != null) {
                    consumer.accept(primary);
                }
                ResourceLocation secondary = plant.getSecondaryPlantTexture(stage);
                if (secondary != null) {
                    consumer.accept(secondary);
                }
            }
            ResourceLocation seed = plant.getSeedTexture();
            if (seed != null) {
                consumer.accept(seed);
            }
        }
    }

}
