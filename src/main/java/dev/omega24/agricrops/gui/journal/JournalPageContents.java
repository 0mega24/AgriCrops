package dev.omega24.agricrops.gui.journal;

import dev.omega24.agricrops.reference.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class JournalPageContents implements JournalPage {

    @Override
    public ResourceLocation getForeground() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/journal/gui_journal_page_toc.png");
    }

}
