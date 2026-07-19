package dev.omega24.agricrops.reference;

/**
 * Mod-wide constants.
 *
 * <p>Previously QuikMod-templated from {@code mod.properties}; now plain literals
 * after the migration to the RetroFuturaGradle build (there is no more build-time
 * token substitution here).
 */
public interface Reference {

    String MOD_NAME = "Agricrops";
    String MOD_ID = "agricrops";
    String AUTHOR = "omega24";

    String VER_MAJOR = "0";
    String VER_MINOR = "1";
    String VER_PATCH = "0";
    String MOD_VERSION = "0.1.0";
    String VERSION = "1.12.2-0.1.0";

    // Lower bounds for the @Mod dependency string. "0.0.0" accepts any version.
    String VERSION_INFLIB = "0.0.0";
    String VERSION_FORGE = "14.23.5.2768";

    String CLIENT_PROXY_CLASS = "dev.omega24.agricrops.proxy.ClientProxy";
    String SERVER_PROXY_CLASS = "dev.omega24.agricrops.proxy.ServerProxy";
    String GUI_FACTORY_CLASS = "dev.omega24.agricrops.gui.GuiFactory";

    String UPDATE_URL = "";

}
