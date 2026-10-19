package dev.omega24.agricrops;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/** Minimal, side-neutral entry point for the new implementation. */
@Mod(modid = Agricrops.MOD_ID, name = "Agricrops", version = "0.2.0-SNAPSHOT",
        acceptedMinecraftVersions = "[1.12.2]")
public final class Agricrops {
    public static final String MOD_ID = "agricrops";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModLog().info("Agricrops Java foundation loaded; gameplay implementation is in progress.");
    }
}
