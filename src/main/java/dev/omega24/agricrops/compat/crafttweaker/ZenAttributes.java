package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.attribute.LockedAttribute;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * ZenScript bridge for {@link LockedAttribute}s and cross-breeding unlock rules
 * (crop-mod-design.md §7, §11 step 8) - the locked-attribute javadoc itself calls out
 * CraftTweaker as the intended authoring path for these, since the attribute set is meant to
 * be open-population rather than a fixed Java enum.
 */
@ZenClass("mods.agricrops.Attributes")
@ZenRegister
public class ZenAttributes {

    /**
     * Registers a new locked attribute (e.g. "frost_hardiness"). {@code bipolar} matches
     * {@link dev.omega24.agricrops.api.v1.stat.CropStat#isBipolar()}'s meaning: bipolar
     * attributes are archetype axes (plus/minus are two different valid directions, not
     * good/bad), magnitude-only ones are "more is generically better at this one thing."
     */
    @ZenMethod
    public static void addAttribute(String id, boolean bipolar) {
        addAttribute(id, bipolar, LockedAttribute.DEFAULT_STABILIZATION_THRESHOLD);
    }

    /**
     * As {@link #addAttribute(String, boolean)}, but with a custom "generations bred true"
     * stabilization threshold (crop-mod-design.md §7) instead of the default of 3 - some
     * attributes should be harder to stabilize than others.
     */
    @ZenMethod
    public static void addAttribute(String id, boolean bipolar, int stabilizationThreshold) {
        CTHelper.apply("Adding agricrops locked attribute: " + id, () -> {
            if (!AgriApi.getAttributeRegistry().add(new LockedAttribute(id, bipolar, stabilizationThreshold))) {
                CraftTweakerAPI.logWarning("agricrops: locked attribute '" + id + "' is already registered, skipping.");
            }
        });
    }

    /**
     * Registers a cross-breeding unlock rule (crop-mod-design.md §7): crossing
     * {@code speciesA} and {@code speciesB} gets a {@code chance}-weighted roll (0.0..1.0) to
     * unlock {@code attributeId} on the offspring's genome, if neither parent already carries
     * it. Both species need a {@code lockedAttributeSlot} for this attribute
     * (see {@code mods.agricrops.Species}) or the rule is meaningless.
     */
    @ZenMethod
    public static void addUnlockRule(String speciesA, String speciesB, String attributeId, double chance) {
        CTHelper.apply("Adding agricrops attribute unlock rule: " + speciesA + " + " + speciesB + " -> " + attributeId, () -> {
            if (!AgriApi.getAttributeUnlockRegistry().addRule(speciesA, speciesB, attributeId, chance)) {
                CraftTweakerAPI.logError("agricrops: invalid chance " + chance + " for unlock rule " + speciesA + " + " + speciesB + " -> " + attributeId + " (must be 0.0..1.0).");
            }
        });
    }

    /**
     * Flags a species pair as hard-incompatible (crop-mod-design.md §7): no cross seed forms
     * between them at all, regardless of stats or luck - distinct from a pair with simply no
     * unlock rule, which can still cross (just without unlocking anything new).
     */
    @ZenMethod
    public static void markIncompatible(String speciesA, String speciesB) {
        CTHelper.apply("Marking agricrops species incompatible: " + speciesA + " + " + speciesB, () -> {
            if (!AgriApi.getAttributeUnlockRegistry().markIncompatible(speciesA, speciesB)) {
                CraftTweakerAPI.logWarning("agricrops: species pair " + speciesA + " + " + speciesB + " is already marked incompatible, skipping.");
            }
        });
    }

}
