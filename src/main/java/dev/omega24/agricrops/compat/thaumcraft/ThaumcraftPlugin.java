package dev.omega24.agricrops.compat.thaumcraft;

import dev.omega24.agricrops.api.v1.plugin.AgriPlugin;
import dev.omega24.agricrops.api.v1.plugin.IAgriPlugin;
import dev.omega24.agricrops.init.AgriBlocks;
import dev.omega24.agricrops.reference.Constants;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;

@AgriPlugin
public class ThaumcraftPlugin implements IAgriPlugin {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getId() {
        return "thaumcraft";
    }

    @Override
    public String getName() {
        return "Thaumcraft Integration";
    }

    @Override
    public void initPlugin() {

        // Fix Golems
        FMLInterModComms.sendMessage(
                "thaumcraft",
                "harvestClickableCrop",
                new ItemStack(AgriBlocks.getInstance().CROP, 1, Constants.MATURE)
        );

    }

}
