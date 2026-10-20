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
        net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(GameContent.RESERVOIR), 0, new ModelResourceLocation("agricrops:reservoir", "inventory"));
        for (Item item : new Item[] {GameContent.SPECIMEN, GameContent.CLIPPING, GameContent.FOUNDATION, GameContent.COMPOST, GameContent.PROBE, GameContent.ANALYZER, GameContent.JOURNAL})
            net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        ModelResourceLocation wheat = new ModelResourceLocation("minecraft:wheat_seeds", "inventory"), beet = new ModelResourceLocation("minecraft:beetroot_seeds", "inventory"), carrot = new ModelResourceLocation("minecraft:carrot", "inventory");
        for (Item carrier : new Item[] {GameContent.SPECIMEN, GameContent.CLIPPING}) {
            net.minecraft.client.renderer.block.model.ModelBakery.registerItemVariants(carrier, wheat, beet, carrot);
            net.minecraftforge.client.model.ModelLoader.setCustomMeshDefinition(carrier, stack -> {
                String profile = stack.hasTagCompound() ? stack.getTagCompound().getString("agricrops_visual") : "wheat";
                return profile.equals("beetroot") ? beet : profile.equals("carrot") ? carrot : wheat;
            });
        }
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().hasTagCompound()
                && event.getItemStack().getTagCompound().hasKey(dev.omega24.agricrops.persistence.CropNbt.ITEM_KEY)) {
            event.getToolTip().add(net.minecraft.util.text.TextFormatting.GRAY + I18n.format("agricrops.tooltip.specimen"));
            if (net.minecraft.client.gui.GuiScreen.isShiftKeyDown()) event.getToolTip().add(I18n.format("agricrops.tooltip.specimen.details"));
        }
        if (event.getItemStack().getItem() == GameContent.PROBE || event.getItemStack().getItem() == GameContent.ANALYZER || event.getItemStack().getItem() == GameContent.JOURNAL)
            event.getToolTip().add(net.minecraft.util.text.TextFormatting.GRAY + I18n.format("agricrops.tooltip.field_tool"));
        if (event.getItemStack().getItem() == Item.getItemFromBlock(GameContent.CROP_STICKS)) {
            event.getToolTip().add(net.minecraft.util.text.TextFormatting.GRAY + I18n.format("agricrops.tooltip.crop_sticks"));
            if (net.minecraft.client.gui.GuiScreen.isShiftKeyDown()) {
                event.getToolTip().add(I18n.format("agricrops.tooltip.crop_sticks.details"));
            } else event.getToolTip().add(I18n.format("agricrops.tooltip.expand"));
        }
    }
}
