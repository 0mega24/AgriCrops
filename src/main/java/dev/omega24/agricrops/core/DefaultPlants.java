/*
 */
package dev.omega24.agricrops.core;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.render.RenderMethod;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Fork-native baseline plants, registered in code (mirrors {@link DefaultSoils}).
 *
 * <p>The upstream AgriCraft default plant JSONs (AgriPlants) were dropped for good in
 * the hard fork. Until the design's species/attribute registries land
 * (crop-mod-design §7), this provides vanilla-equivalent crops so crop sticks have
 * something to grow. Growth/seed chances here are arbitrary placeholders, not balanced
 * numbers.
 *
 * <p>Cactus/carrot/potato/sugarcane/melon/pumpkin reuse leftover AgriCraft-style art
 * already vendored under {@code assets/agricrops}. Wheat, beetroot, and nether wart have
 * no fork-native art, so they borrow the vanilla block/item textures directly — they
 * were vanilla crops to begin with.
 *
 * <p>Each plant also accepts its vanilla (or vanilla-equivalent) item as an "unverified"
 * seed — same as upstream AgriCraft: planting a plain vanilla wheat seed directly on a
 * crop stick works, it just starts unanalyzed until run through the Seed Analyzer.
 */
public final class DefaultPlants {

    private DefaultPlants() {
    }

    public static void register() {
        forkTextured("cactus", "Cactus", Collections.singletonList("sand"),
                new ItemStack(Item.getItemFromBlock(Blocks.CACTUS)),
                new ItemStack(Item.getItemFromBlock(Blocks.CACTUS)));
        forkTextured("carrot", "Carrot", Collections.singletonList("farmland"),
                new ItemStack(Items.CARROT), new ItemStack(Items.CARROT));
        forkTextured("potato", "Potato", Collections.singletonList("farmland"),
                new ItemStack(Items.POTATO), new ItemStack(Items.POTATO));
        forkTextured("sugarcane", "Sugar Cane", Arrays.asList("sand", "dirt", "grass", "farmland"),
                new ItemStack(Items.REEDS), new ItemStack(Items.REEDS));
        forkTextured("melon", "Melon", Arrays.asList("farmland", "dirt", "grass"),
                new ItemStack(Items.MELON), new ItemStack(Items.MELON_SEEDS));
        forkTextured("pumpkin", "Pumpkin", Arrays.asList("farmland", "dirt", "grass"),
                new ItemStack(Item.getItemFromBlock(Blocks.PUMPKIN)), new ItemStack(Items.PUMPKIN_SEEDS));

        vanillaTextured("wheat", "Wheat", Collections.singletonList("farmland"),
                8, "wheat_stage", "wheat_seeds", new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT_SEEDS));
        vanillaTextured("beetroot", "Beetroot", Collections.singletonList("farmland"),
                4, "beetroots_stage", "beetroot_seeds", new ItemStack(Items.BEETROOT), new ItemStack(Items.BEETROOT_SEEDS));
        vanillaTextured("nether_wart", "Nether Wart", Collections.singletonList("soul_sand"),
                3, "nether_wart_stage_", "nether_wart", new ItemStack(Items.NETHER_WART), new ItemStack(Items.NETHER_WART));
    }

    private static void forkTextured(String id, String name, List<String> soils, ItemStack product, ItemStack unverifiedSeed) {
        register(new CodePlant(id, name, soils, 4, RenderMethod.CROSSED,
                stage -> new ResourceLocation("agricrops:blocks/crop_" + id + (stage + 1)),
                new ResourceLocation("agricrops:items/seed_" + id),
                Collections.singletonList(product), Collections.singletonList(unverifiedSeed)));
    }

    private static void vanillaTextured(String id, String name, List<String> soils, int stages,
                                        String blockStagePrefix, String seedItemName, ItemStack product,
                                        ItemStack unverifiedSeed) {
        register(new CodePlant(id, name, soils, stages, RenderMethod.CROSSED,
                stage -> new ResourceLocation("minecraft:blocks/" + blockStagePrefix + stage),
                new ResourceLocation("minecraft:items/" + seedItemName),
                Collections.singletonList(product), Collections.singletonList(unverifiedSeed)));
    }

    private static void register(CodePlant plant) {
        AgriApi.getPlantRegistry().add(plant);
        AgriCore.getLogger("agricrops").info("Registered fork-native plant: {0}", plant.getId());
    }

}
