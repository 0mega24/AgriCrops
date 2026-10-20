package dev.omega24.agricrops.world;

import dev.omega24.agricrops.Agricrops;
import dev.omega24.agricrops.content.GameContent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;

/** Permit our explicit sneak controls without overriding another mod's cancellation/use denial. */
@Mod.EventBusSubscriber(modid = Agricrops.MOD_ID)
public final class FieldInteractions {
    private FieldInteractions() {}
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void clicked(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getUseBlock() == Event.Result.DENY || !event.getEntityPlayer().isSneaking()
                || !event.getWorld().isBlockLoaded(event.getPos()) || !(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof CropSticksBlock)) return;
        ItemStack held = event.getItemStack();
        if (held.isEmpty() || held.getItem() == Items.STICK || held.getItem() == GameContent.PROBE || held.getItem() == GameContent.ANALYZER
                || held.getItem() == GameContent.JOURNAL || held.getItem() == Items.DYE && held.getMetadata() == 15) event.setUseBlock(Event.Result.ALLOW);
    }
}
