package dev.omega24.agricrops.items.tabs;

import dev.omega24.agricrops.init.AgriItems;
import dev.omega24.agricrops.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class AgriTabs {

    public static final CreativeTabs TAB_AGRICRAFT = new CreativeTabs(Reference.MOD_ID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(AgriItems.getInstance().DEBUGGER);
        }
    };

    public static final CreativeTabs TAB_AGRICRAFT_SEED = new CreativeTabs(Reference.MOD_ID + "_seeds") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(AgriItems.getInstance().AGRI_SEED);
        }
    };

}
