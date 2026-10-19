package dev.omega24.agricrops.client;

import dev.omega24.agricrops.Agricrops;
import dev.omega24.agricrops.content.GameContent;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Agricrops.MOD_ID, value = Side.CLIENT)
public final class ClientContent {
    private ClientContent() {}
    @SubscribeEvent public static void models(ModelRegistryEvent event) {
        net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GameContent.CROP_STICKS), 0,
                new ModelResourceLocation("agricrops:crop_sticks", "inventory"));
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().hasTagCompound()
                && event.getItemStack().getTagCompound().hasKey(dev.omega24.agricrops.persistence.CropNbt.ITEM_KEY)) {
            event.getToolTip().add(net.minecraft.util.text.TextFormatting.GRAY + I18n.format("agricrops.tooltip.specimen"));
        }
        if (event.getItemStack().getItem() == Item.getItemFromBlock(GameContent.CROP_STICKS)) {
            event.getToolTip().add(net.minecraft.util.text.TextFormatting.GRAY + I18n.format("agricrops.tooltip.crop_sticks"));
            if (net.minecraft.client.gui.GuiScreen.isShiftKeyDown()) {
                event.getToolTip().add(I18n.format("agricrops.tooltip.crop_sticks.details"));
            } else event.getToolTip().add(I18n.format("agricrops.tooltip.expand"));
        }
    }
}
