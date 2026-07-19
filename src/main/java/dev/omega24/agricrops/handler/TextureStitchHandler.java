package dev.omega24.agricrops.handler;

import dev.omega24.agricrops.impl.v1.PluginHandler;
import dev.omega24.agricrops.core.CoreHandler;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "agricrops", value = Side.CLIENT)
public class TextureStitchHandler {

    private TextureStitchHandler() {
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public static void onTextureStitch(TextureStitchEvent e) {
        CoreHandler.loadTextures(e.getMap()::registerSprite);
        PluginHandler.loadTextures(e.getMap()::registerSprite);
    }
}
