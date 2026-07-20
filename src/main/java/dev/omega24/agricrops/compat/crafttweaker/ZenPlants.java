package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * ZenScript bridge for {@link dev.omega24.agricrops.core.CodePlant} registration
 * (crop-mod-design.md §11 step 8, replacing the dead {@code JsonPlant}/AgriCore-JSON pipeline
 * as the pack-dev-facing way to define a plant - see {@code ZenPlantBuilder} for the actual
 * configuration surface).
 */
@ZenClass("mods.agricrops.Plants")
@ZenRegister
public class ZenPlants {

    /**
     * Starts building a new plant with the given id (e.g. "my_pack:frostberry") and display
     * name. Call {@code .register()} on the returned builder once configured - nothing is
     * registered until then.
     */
    @ZenMethod
    public static ZenPlantBuilder builder(String id, String name) {
        return new ZenPlantBuilder(id, name);
    }

}
