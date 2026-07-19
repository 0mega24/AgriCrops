package dev.omega24.agricrops.items;

import dev.omega24.agricrops.AgriCraft;
import dev.omega24.agricrops.items.modes.*;
import dev.omega24.agricrops.items.tabs.AgriTabs;
import dev.omega24.agricrops.reference.AgriCraftConfig;
import com.infinityraider.infinitylib.item.IItemWithModel;
import com.infinityraider.infinitylib.item.ItemDebuggerBase;
import com.infinityraider.infinitylib.utility.debug.DebugMode;
import java.util.ArrayList;
import java.util.List;

public class ItemDebugger extends ItemDebuggerBase implements IItemWithModel {

    public ItemDebugger() {
        super(true);
        this.setMaxStackSize(1);
        if (AgriCraftConfig.debug) {
            this.setCreativeTab(AgriTabs.TAB_AGRICRAFT);
        }
    }

    @Override
    protected List<DebugMode> getDebugModes() {
        List<DebugMode> list = new ArrayList<>();
        list.add(new DebugModeCheckSoil());
        list.add(new DebugModeClearGrass());
        list.add(new DebugModeCoreInfo());
        list.add(new DebugModeTestBlockRange()); // Just for temporary testing.
        list.add(new DebugModeIGrowable());
        list.add(new DebugModeDiffLight());
        list.add(new DebugModeFillComponent());
        return list;
    }
}
