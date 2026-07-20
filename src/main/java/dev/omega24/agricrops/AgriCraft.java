package dev.omega24.agricrops;

import dev.omega24.agricrops.init.AgriBlocks;
import dev.omega24.agricrops.init.AgriCommands;
import dev.omega24.agricrops.init.AgriItems;
import dev.omega24.agricrops.network.MessageContainerSeedStorage;
import dev.omega24.agricrops.network.MessageFertilizerApplied;
import dev.omega24.agricrops.network.MessageCompareLight;
import dev.omega24.agricrops.network.MessageGuiSeedStorageClearSeed;
import dev.omega24.agricrops.network.MessageTileEntitySeedStorage;
import dev.omega24.agricrops.network.json.MessageSyncMutationJson;
import dev.omega24.agricrops.network.json.MessageSyncPlantJson;
import dev.omega24.agricrops.proxy.IProxy;
import dev.omega24.agricrops.reference.AgriAlphaWarnings;
import dev.omega24.agricrops.reference.Reference;
import com.infinityraider.infinitylib.InfinityMod;
import com.infinityraider.infinitylib.config.IModConfiguration;
import com.infinityraider.infinitylib.config.ModConfiguration;
import com.infinityraider.infinitylib.network.INetworkWrapper;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <p>
 * This is my first "real" mod, I've made this while learning to use Minecraft Forge to Mod
 * Minecraft. The code might not be optimal but that wasn't the point of this project.
 * </p>
 * Cheers to:
 * <ul>
 * <li> Pam for trusting me with her source code and support. </li>
 * <li> Pahimar for making his code open source and for creating his Let's Mod Reboot Youtube
 * series, I've learned a lot from this (also used some code, credit's due where credit's due).
 * </li>
 * <li> VSWE for his "Forging a Minecraft Mod" summer courses. </li>
 * <li> NealeGaming for his Minecraft modding tutorials on youtube. </li>
 * <li> Imasius (a.k.a. Nimo) for learning me to better code in java. </li>
 * <li> RlonRyan for helping out with the code. </li>
 * <li> HenryLoenwind for the API. </li>
 * <li> MechWarrior99, SkullyGamingMC, VapourDrive and SkeletonPunk for providing textures. </li>
 * </ul>
 *
 * I've annotated my code heavily, for myself and for possible others who might learn from it.
 * <br>
 * Oh and keep on modding in the free world!
 * <p>
 * ~ InfinityRaider
 * </p>
 *
 * @author InfinityRaider
 */
@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.MOD_VERSION,
        guiFactory = Reference.GUI_FACTORY_CLASS,
        updateJSON = Reference.UPDATE_URL,
        dependencies
        = "required-after:forge@[" + Reference.VERSION_FORGE + ",);"
        + "required-after:infinitylib@[" + Reference.VERSION_INFLIB + ",);"
)
public class AgriCraft extends InfinityMod {

    @Mod.Instance(Reference.MOD_ID)
    public static AgriCraft instance;

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;

    @Override
    public IProxy proxy() {
        return proxy;
    }

    @Override
    public String getModId() {
        return Reference.MOD_ID;
    }

    @Override
    public Object getModBlockRegistry() {
        return AgriBlocks.getInstance();
    }

    @Override
    public Object getModItemRegistry() {
        return AgriItems.getInstance();
    }
    
    public Object getModCommandRegistry() {
        return AgriCommands.getInstance();
    }

    @Override
    public void registerMessages(INetworkWrapper wrapper) {
        wrapper.registerMessage(MessageContainerSeedStorage.class);
        wrapper.registerMessage(MessageFertilizerApplied.class);
        wrapper.registerMessage(MessageGuiSeedStorageClearSeed.class);
        wrapper.registerMessage(MessageTileEntitySeedStorage.class);
        wrapper.registerMessage(MessageSyncPlantJson.class);
        wrapper.registerMessage(MessageSyncMutationJson.class);
        wrapper.registerMessage(MessageCompareLight.class);
    }

    @Override
    public IModConfiguration getConfiguration() {
        return ModConfiguration.getInstance();
    }
    
}
