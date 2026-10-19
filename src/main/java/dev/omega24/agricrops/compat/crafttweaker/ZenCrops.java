package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.agricrops.Crops")
public final class ZenCrops {
    private ZenCrops() {}

    @ZenMethod
    public static ZenCropBuilder builder(String id, String seedItem, String productItem, String nameKey) {
        return new ZenCropBuilder(id, seedItem, productItem, nameKey);
    }
}
