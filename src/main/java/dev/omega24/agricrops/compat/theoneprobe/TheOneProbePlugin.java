package dev.omega24.agricrops.compat.theoneprobe;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.plugin.AgriPlugin;
import dev.omega24.agricrops.api.v1.plugin.IAgriPlugin;
import net.minecraftforge.fml.common.event.FMLInterModComms;

@AgriPlugin
public class TheOneProbePlugin implements IAgriPlugin {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getId() {
        return "theoneprobe";
    }

    @Override
    public String getName() {
        return "The One Probe Integration";
    }

    @Override
    public void initPlugin() {
        AgriCore.getLogger("agricrops").debug("Calling One Probe Register! Result: {0}!",
                FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", this.getClass().getPackage().getName() + ".GetTheOneProbe"));
    }

}
