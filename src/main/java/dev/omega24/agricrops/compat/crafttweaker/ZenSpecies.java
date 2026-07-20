package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.attribute.LockedAttribute;
import dev.omega24.agricrops.api.v1.species.CropSpecies;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * ZenScript bridge for {@link CropSpecies} (crop-mod-design.md §7, §11 step 8). By convention a
 * species id must match the {@code plantId} it backs (one species per plant) - see
 * {@code mods.agricrops.Plants} for registering the plant itself.
 */
@ZenClass("mods.agricrops.Species")
@ZenRegister
public class ZenSpecies {

    /**
     * Registers a "wild-type" species for {@code plantId} - all-zero base stats, capable of
     * unlocking each attribute id in {@code lockedAttributeIds} (which must already be
     * registered via {@code mods.agricrops.Attributes.addAttribute}).
     */
    @ZenMethod
    public static void register(String plantId, String[] lockedAttributeIds) {
        register(plantId, new int[CropStat.values().length], lockedAttributeIds);
    }

    /**
     * As {@link #register(String, String[])}, but with a custom base stat template - the
     * default rolls a fresh, unbred genome of this species gets for each of the 8 standard
     * stats (crop-mod-design.md §2), in order: growth, yield, resilience, water_affinity,
     * fertility_response, light_tolerance, temp_tolerance, mutation_chance. Each in
     * -63..63; out-of-range values are clamped.
     */
    @ZenMethod
    public static void register(String plantId, int[] baseStats, String[] lockedAttributeIds) {
        CTHelper.apply("Registering agricrops species: " + plantId, () -> {
            final CropSpecies.Builder builder = CropSpecies.builder(plantId);
            final CropStat[] stats = CropStat.values();
            for (int i = 0; i < stats.length && i < baseStats.length; i++) {
                builder.baseStat(stats[i], baseStats[i]);
            }
            for (String attributeId : lockedAttributeIds) {
                final LockedAttribute attribute = AgriApi.getAttributeRegistry().get(attributeId).orElse(null);
                if (attribute == null) {
                    CraftTweakerAPI.logError("agricrops: no locked attribute '" + attributeId + "' registered, skipping slot on species " + plantId + ".");
                    continue;
                }
                builder.lockedAttributeSlot(attribute);
            }
            if (!AgriApi.getSpeciesRegistry().add(builder.build())) {
                CraftTweakerAPI.logWarning("agricrops: species '" + plantId + "' is already registered, skipping.");
            }
        });
    }

}
