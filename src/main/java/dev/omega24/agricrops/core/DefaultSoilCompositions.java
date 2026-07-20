/*
 */
package dev.omega24.agricrops.core;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.AgriApi;

/**
 * Fork-native base soil composition tags (crop-mod-design.md §3), registered in code.
 * Open registry: further tags (e.g. GT-ore-bearing soil) can be added later without
 * touching this class.
 */
public final class DefaultSoilCompositions {

    private DefaultSoilCompositions() {
    }

    public static void register() {
        tag("dirt", "Dirt");
        tag("grass", "Grass");
        tag("tilled", "Tilled Earth");
        tag("sand", "Sand");
        tag("gravel", "Gravel");
        tag("clay", "Clay");
        tag("stone", "Stone");
        tag("soul_sand", "Soul Sand");
    }

    private static void tag(String id, String name) {
        AgriApi.getSoilCompositionRegistry().add(new SoilComposition(id, name));
        AgriCore.getLogger("agricrops").info("Registered soil composition tag: {0}", id);
    }

}
