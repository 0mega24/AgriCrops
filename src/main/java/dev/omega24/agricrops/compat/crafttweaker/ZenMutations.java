package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import dev.omega24.agricrops.api.v1.AgriApi;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * ZenScript bridge for the seed-cross-breeding mutation registry (crop-mod-design.md §6/§7,
 * §11 step 8) - the actual "these parents in adjacent crop sticks can produce this child"
 * relation, distinct from {@code mods.agricrops.Attributes}' unlock rules (which only govern
 * whether a cross also unlocks a locked attribute, not whether the cross itself is legal).
 */
@ZenClass("mods.agricrops.Mutations")
@ZenRegister
public class ZenMutations {

    /**
     * Registers a mutation: having every plant in {@code parentIds} planted in adjacent crop
     * sticks around an empty cross-crop gets a {@code chance}-weighted roll (0.0..1.0) to grow
     * {@code childId} there instead.
     */
    @ZenMethod
    public static void addMutation(String id, double chance, String childId, String[] parentIds) {
        CTHelper.apply("Adding agricrops mutation: " + id, () -> {
            if (!AgriApi.getMutationRegistry().add(id, chance, childId, parentIds)) {
                CraftTweakerAPI.logWarning("agricrops: mutation '" + id + "' is already registered, skipping.");
            }
        });
    }

}
