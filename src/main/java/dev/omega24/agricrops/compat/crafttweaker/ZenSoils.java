package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import dev.omega24.agricrops.core.SoilComposition;
import dev.omega24.agricrops.impl.v1.SoilProfile;
import net.minecraft.block.Block;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * ZenScript bridge for soil compositions/profiles (crop-mod-design.md §3, §11 step 8).
 */
@ZenClass("mods.agricrops.Soils")
@ZenRegister
public class ZenSoils {

    /**
     * Registers a new soil composition tag (e.g. "peat"), for use by
     * {@code mods.agricrops.Plants} and {@code addProfile}.
     */
    @ZenMethod
    public static void addComposition(String id, String name) {
        CTHelper.apply("Adding agricrops soil composition: " + id, () -> {
            if (!AgriApi.getSoilCompositionRegistry().add(new SoilComposition(id, name))) {
                CraftTweakerAPI.logWarning("agricrops: soil composition '" + id + "' is already registered, skipping.");
            }
        });
    }

    /**
     * Registers {@code blockRegistryName} (e.g. "minecraft:dirt") as a soil surface with the
     * given composition tag and growth-mechanics numbers (crop-mod-design.md §3).
     *
     * @param nutrientCapacity max size of a {@code SoilColumn}'s nutrient pool on this surface.
     * @param moistureRetention 0..1 - how much of the per-tick moisture decay this surface
     * softens (1.0 = no decay, 0.0 = full decay).
     * @param growthMultiplier flat growth-chance multiplier for this surface.
     */
    @ZenMethod
    public static void addProfile(String blockRegistryName, String compositionId, int nutrientCapacity,
                                   float moistureRetention, float growthMultiplier) {
        CTHelper.apply("Adding agricrops soil profile for: " + blockRegistryName, () -> {
            final Block block = Block.getBlockFromName(blockRegistryName);
            if (block == null) {
                CraftTweakerAPI.logError("agricrops: no block found with registry name '" + blockRegistryName + "', skipping soil profile.");
                return;
            }
            final ISoilComposition composition = AgriApi.getSoilCompositionRegistry().get(compositionId).orElse(null);
            if (composition == null) {
                CraftTweakerAPI.logError("agricrops: no soil composition '" + compositionId + "' registered (call addComposition first), skipping soil profile for " + blockRegistryName + ".");
                return;
            }
            final ISoilProfile profile = new SoilProfile(composition, nutrientCapacity, moistureRetention, growthMultiplier);
            if (!AgriApi.getSoilProfileRegistry().register(block, profile)) {
                CraftTweakerAPI.logWarning("agricrops: block '" + blockRegistryName + "' already has a soil profile, skipping.");
            }
        });
    }

}
