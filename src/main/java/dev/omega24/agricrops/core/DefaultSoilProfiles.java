/*
 */
package dev.omega24.agricrops.core;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import dev.omega24.agricrops.impl.v1.SoilProfile;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Fork-native base soil profiles (crop-mod-design.md §3), registered in code. Replaces
 * the old item/block-matching soil registry stand-in now that the real
 * {@code ISoilProfileRegistry} has landed.
 *
 * <p>Numbers here are placeholders, not balanced values — matching the worked examples in
 * §3: grass/dirt/farmland stay close to vanilla behavior, sand gets low nutrient capacity
 * and fast moisture drain, clay gets strong moisture retention but slow nutrient uptake,
 * and stone gets minimal nutrient capacity and no moisture retention (viable only for
 * species that specifically tolerate it, once species preference lands in §11 step 5).
 */
public final class DefaultSoilProfiles {

    private DefaultSoilProfiles() {
    }

    public static void register() {
        profile(Blocks.DIRT, "dirt", 100, 0.5f, 1.0f);
        profile(Blocks.GRASS, "grass", 100, 0.5f, 1.0f);
        profile(Blocks.FARMLAND, "tilled", 150, 0.6f, 1.1f);
        profile(Blocks.SAND, "sand", 40, 0.2f, 0.9f);
        profile(Blocks.GRAVEL, "gravel", 30, 0.2f, 0.8f);
        profile(Blocks.CLAY, "clay", 80, 0.9f, 0.7f);
        profile(Blocks.STONE, "stone", 10, 0.0f, 0.5f);
        profile(Blocks.SOUL_SAND, "soul_sand", 60, 0.4f, 1.0f);
    }

    private static void profile(Block block, String compositionId, int nutrientCapacity,
                    float moistureRetention, float growthMultiplier) {
        final ISoilComposition composition = AgriApi.getSoilCompositionRegistry().get(compositionId)
                .orElseThrow(() -> new IllegalStateException("Unknown soil composition tag: " + compositionId));
        AgriApi.getSoilProfileRegistry().register(block,
                new SoilProfile(composition, nutrientCapacity, moistureRetention, growthMultiplier));
        AgriCore.getLogger("agricrops").info("Registered soil profile for block: {0} ({1})", block, compositionId);
    }

}
