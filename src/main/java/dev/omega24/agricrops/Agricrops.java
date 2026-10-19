package dev.omega24.agricrops;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.world.CropTile;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

/** Minimal, side-neutral entry point for the new implementation. */
@Mod(modid = Agricrops.MOD_ID, name = "Agricrops", version = "0.2.0-SNAPSHOT",
        acceptedMinecraftVersions = "[1.12.2]", dependencies = "after:crafttweaker")
public final class Agricrops {
    public static final String MOD_ID = "agricrops";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(CropTile.class, new ResourceLocation(MOD_ID, "crop_sticks"));
        event.getModLog().info("Agricrops: collecting Java/CraftTweaker crop definitions.");
    }

    @Mod.EventHandler public void postInit(FMLPostInitializationEvent event) { CropDefinitions.freeze(); }

    @Mod.EventHandler public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new dev.omega24.agricrops.command.AgricropsCommand());
    }
}
