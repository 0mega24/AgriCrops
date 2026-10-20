package dev.omega24.agricrops.content;

import dev.omega24.agricrops.core.genetics.Specimen;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
import dev.omega24.agricrops.persistence.CropNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Generic carriers cannot be planted as vanilla crops and silently lose genetics. */
public final class SpecimenItems {
    private SpecimenItems() {}
    public static ItemStack of(Specimen specimen) {
        ItemStack stack = new ItemStack(specimen.origin == SeedOrigin.CLIPPING ? GameContent.CLIPPING : GameContent.SPECIMEN);
        NBTTagCompound tag = new NBTTagCompound(); tag.setTag(CropNbt.ITEM_KEY, CropNbt.seed(specimen));
        tag.setString("agricrops_visual", CropDefinitions.get(specimen.body).rules().visual); stack.setTagCompound(tag); return stack;
    }
    public static Specimen read(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(CropNbt.ITEM_KEY, 10)) throw new IllegalArgumentException("Missing specimen");
        Specimen specimen = CropNbt.read(stack.getTagCompound().getCompoundTag(CropNbt.ITEM_KEY)).specimen();
        PocContent.validate(specimen, CropDefinitions.all()); return specimen;
    }
}
