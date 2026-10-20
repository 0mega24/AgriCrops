package dev.omega24.agricrops.content;

import dev.omega24.agricrops.Agricrops;
import dev.omega24.agricrops.world.CropSticksBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Agricrops.MOD_ID)
public final class GameContent {
    public static final CropSticksBlock CROP_STICKS = new CropSticksBlock();
    public static final dev.omega24.agricrops.world.ReservoirBlock RESERVOIR = new dev.omega24.agricrops.world.ReservoirBlock();
    public static final Item SPECIMEN = item("specimen_seed"), CLIPPING = item("clipping"), FOUNDATION = item("foundation_seed"),
            COMPOST = item("compost"), PROBE = item("probe"), ANALYZER = item("analyzer"), JOURNAL = item("journal");
    private static Item item(String name) { return new Item().setRegistryName("agricrops", name).setUnlocalizedName("agricrops." + name).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC).setMaxStackSize(1); }
    private GameContent() {}
    @SubscribeEvent public static void blocks(RegistryEvent.Register<Block> event) { event.getRegistry().registerAll(CROP_STICKS, RESERVOIR); }
    @SubscribeEvent public static void items(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(CROP_STICKS).setRegistryName(CROP_STICKS.getRegistryName()));
        event.getRegistry().register(new ItemBlock(RESERVOIR).setRegistryName(RESERVOIR.getRegistryName()));
        event.getRegistry().registerAll(SPECIMEN, CLIPPING, FOUNDATION, COMPOST, PROBE, ANALYZER, JOURNAL);
    }
    @SubscribeEvent public static void recipes(RegistryEvent.Register<net.minecraft.item.crafting.IRecipe> event) {
        net.minecraftforge.fml.common.registry.GameRegistry.addShapedRecipe(new net.minecraft.util.ResourceLocation("agricrops:reservoir"), null, new net.minecraft.item.ItemStack(RESERVOIR), "p p", "pbp", "ppp", 'p', net.minecraft.init.Blocks.PLANKS, 'b', net.minecraft.init.Items.BUCKET);
        recipe("foundation_seed", FOUNDATION, net.minecraft.init.Items.WHEAT, new net.minecraft.item.ItemStack(net.minecraft.init.Items.DYE, 1, 15));
        recipe("compost", COMPOST, net.minecraft.init.Items.WHEAT, net.minecraft.init.Items.WHEAT, net.minecraft.init.Items.BEETROOT);
        recipe("probe", PROBE, net.minecraft.init.Items.STICK, net.minecraft.init.Items.IRON_INGOT);
        recipe("analyzer", ANALYZER, PROBE, net.minecraft.init.Items.GLASS_BOTTLE, net.minecraft.init.Items.REDSTONE);
        recipe("journal", JOURNAL, net.minecraft.init.Items.BOOK, net.minecraft.init.Items.WHEAT_SEEDS);
    }
    private static void recipe(String name, Item output, Object... ingredients) {
        net.minecraft.item.crafting.Ingredient[] parsed = new net.minecraft.item.crafting.Ingredient[ingredients.length];
        for (int i = 0; i < ingredients.length; i++) parsed[i] = net.minecraft.item.crafting.Ingredient.fromStacks(ingredients[i] instanceof net.minecraft.item.ItemStack ? (net.minecraft.item.ItemStack) ingredients[i] : new net.minecraft.item.ItemStack((Item) ingredients[i]));
        net.minecraftforge.fml.common.registry.GameRegistry.addShapelessRecipe(new net.minecraft.util.ResourceLocation("agricrops:" + name), null, new net.minecraft.item.ItemStack(output), parsed);
    }
}
