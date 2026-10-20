package dev.omega24.agricrops.content;

import dev.omega24.agricrops.core.content.CropDefinition;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;

/** Explicit source item templates; never infer modded crop metadata, NBT, gates or model suffixes. */
public final class NativeItems {
    private NativeItems() {}
    public static NBTTagCompound template(String text) {
        if (text.isEmpty()) return null;
        try { return JsonToNBT.getTagFromJson(text); } catch (NBTException exception) { throw new IllegalArgumentException("Invalid native item NBT", exception); }
    }
    public static boolean seed(CropDefinition definition, ItemStack stack) {
        return stack.getItem() == Item.REGISTRY.getObject(new ResourceLocation(definition.seed()))
                && stack.getMetadata() == definition.rules().seedMeta
                && NBTUtil.areNBTEquals(template(definition.rules().seedNbt), stack.getTagCompound(), true);
    }
    public static ItemStack product(CropDefinition definition, int count) {
        ItemStack stack = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(definition.product())), count, definition.rules().productMeta);
        stack.setTagCompound(template(definition.rules().productNbt)); return stack;
    }
}
