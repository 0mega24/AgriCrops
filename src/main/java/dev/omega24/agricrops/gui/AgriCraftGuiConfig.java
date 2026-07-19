package dev.omega24.agricrops.gui;

import com.agricrops.agricore.config.AgriConfigCategory;
import com.agricrops.agricore.core.AgriCore;
import dev.omega24.agricrops.core.CoreHandler;
import dev.omega24.agricrops.reference.Reference;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AgriCraftGuiConfig extends GuiConfig {

    public AgriCraftGuiConfig(GuiScreen guiScreen) {
        super(guiScreen, getConfigElements(), Reference.MOD_ID, false, false,
                GuiConfig.getAbridgedConfigPath(AgriCore.getConfig().getLocation()));
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> configElements = new ArrayList<>();
        for (AgriConfigCategory e : AgriConfigCategory.values()) {
            String descr = "AgriCraft " + e.getDisplayName() + " Settings";
            String name = "agricrops.configgui.ctgy." + e.name();
            configElements.add(new DummyConfigElement.DummyCategoryElement(descr, name, new ConfigElement(CoreHandler.getConfig().getCategory(e.name().toLowerCase())).getChildElements()));
        }
        return configElements;
    }
}
