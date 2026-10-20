package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import dev.omega24.agricrops.content.*;
import dev.omega24.agricrops.core.environment.Factor;
import dev.omega24.agricrops.core.field.SoilProfile;
import dev.omega24.agricrops.core.genetics.*;
import stanhebben.zenscript.annotations.*;

@ZenRegister @ZenClass("mods.agricrops.Ecology")
public final class ZenEcology {
    private ZenEcology() {}
    private static void submit(Runnable action) {
        try { action.run(); } catch (RuntimeException exception) {
            String error = CraftTweakerAPI.getScriptFileAndLine() + ": " + exception.getMessage();
            CropDefinitions.error(error); CraftTweakerAPI.logError(error);
        }
    }
    @ZenMethod public static void constantFactor(String id, double min, double max, String unit, double value, boolean replace) {
        submit(() -> PocContent.factor(new Factor(id, min, max, unit, value), replace));
    }
    @ZenMethod public static void biomeFactor(String biome, String factor, double value) { submit(() -> PocContent.biome(biome, factor, value)); }
    @ZenMethod public static void dimensionFactor(int dimension, String factor, double value) { submit(() -> PocContent.dimension(dimension, factor, value)); }
    @ZenMethod public static void soil(String block, int waterCapacity, int nutrientCapacity, int evaporation, int drainage,
                                       int release, int uptake, double saturation, boolean replace) {
        submit(() -> PocContent.soil(new SoilProfile(block, waterCapacity, nutrientCapacity, evaporation, drainage, release, uptake, saturation), replace));
    }
    @ZenMethod public static void trait(String id, String locus, String effect, String factor, double strength,
                                        int dominance, int generations, String nameKey, boolean replace) {
        submit(() -> PocContent.trait(new TraitDefinition(id, locus, TraitDefinition.Effect.valueOf(effect.toUpperCase(java.util.Locale.ROOT)), factor, strength, dominance, generations, nameKey), replace));
    }
    @ZenMethod public static void unlock(String donor, String recipient, String trait) { submit(() -> PocContent.unlock(new Breeding.Unlock(donor, recipient, trait))); }
}
