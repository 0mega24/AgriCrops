/*
 */
package dev.omega24.agricrops.core;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/**
 * Fork-native base soils, registered in code.
 *
 * <p>The upstream AgriCraft default soil JSONs (AgriPlants) were dropped in the hard
 * fork. Until the design's data-driven soil registry lands (crop-mod-design §3), this
 * provides a minimal set so crop sticks are placeable. Each entry here is a natural
 * candidate for a real {@code SoilProfile} later. Note: farmland is intentionally not
 * included — it has no item form, so it cannot be represented as a {@link FuzzyStack}.
 */
public final class DefaultSoils {

    private DefaultSoils() {
    }

    public static void register() {
        // ignoreMeta so every metadata variant of a block counts as the same soil
        // (dirt / coarse dirt / podzol; sand / red sand).
        soil("dirt", "Dirt", Blocks.DIRT);
        soil("grass", "Grass", Blocks.GRASS);
        soil("sand", "Sand", Blocks.SAND);
        soil("gravel", "Gravel", Blocks.GRAVEL);
        // Farmland has no item form, so it can't be a FuzzyStack — match it by block identity.
        blockSoil("farmland", "Farmland", Blocks.FARMLAND);
        soil("soul_sand", "Soul Sand", Blocks.SOUL_SAND);
    }

    private static void soil(String id, String name, Block block) {
        final FuzzyStack variant = new FuzzyStack(new ItemStack(block), true, false);
        AgriApi.getSoilRegistry().add(new CodeSoil(id, name, Collections.singletonList(variant)));
        AgriCore.getLogger("agricrops").info("Registered fork-native soil: {0}", id);
    }

    private static void blockSoil(String id, String name, Block block) {
        AgriApi.getSoilRegistry().add(new CodeSoil(id, name, Collections.emptyList(), Collections.singleton(block)));
        AgriCore.getLogger("agricrops").info("Registered fork-native soil: {0}", id);
    }

}
