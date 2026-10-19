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
    private GameContent() {}
    @SubscribeEvent public static void blocks(RegistryEvent.Register<Block> event) { event.getRegistry().register(CROP_STICKS); }
    @SubscribeEvent public static void items(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(CROP_STICKS).setRegistryName(CROP_STICKS.getRegistryName()));
    }
}
