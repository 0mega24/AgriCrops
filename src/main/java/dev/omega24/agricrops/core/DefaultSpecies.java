package dev.omega24.agricrops.core;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.attribute.LockedAttribute;
import dev.omega24.agricrops.api.v1.species.CropSpecies;
import java.util.Arrays;

/**
 * Fork-native species registrations (crop-mod-design.md §7), registered in code the same way
 * {@link DefaultPlants} registers its baseline plants — one {@link CropSpecies} per baseline
 * plant id, keeping the plant registry and species registry in lock-step by convention (species
 * id == plant id).
 *
 * <p>The baseline plants themselves stay flavorless "vanilla-equivalent" crops (see {@link
 * DefaultPlants}), so their base stat templates are all zero — wild-type, nothing special. Two
 * demo {@link LockedAttribute}s and a couple of unlock/incompatibility rules are registered here
 * too, purely so the registries aren't empty scaffolding: real balancing (and any actually
 * interesting attributes) is for whenever the ZenScript-exposed per-crop definitions land per
 * design §11/§12, not this placeholder set.
 */
public final class DefaultSpecies {

    /**
     * Demo locked attribute: a fifth "hidden" trait wheat and beetroot can jointly unlock.
     * Bipolar like Light/Temp Tolerance (crop-mod-design.md §2) since "which direction" reads
     * more interesting than a flat magnitude bonus for a first example.
     */
    public static final LockedAttribute FROST_HARDINESS = new LockedAttribute("frost_hardiness", true);

    private DefaultSpecies() {
    }

    public static void register() {
        AgriApi.getAttributeRegistry().add(FROST_HARDINESS);

        Arrays.asList("cactus", "carrot", "potato", "sugarcane", "melon", "pumpkin", "wheat", "beetroot", "nether_wart")
                .forEach(DefaultSpecies::registerPlainSpecies);

        // Demo unlock rule: crossing wheat and beetroot has a modest chance to unlock Frost
        // Hardiness on the cross seed, if neither parent already carries it. Both species need
        // the attribute on their locked-attribute slot list, or the rule is meaningless.
        AgriApi.getAttributeUnlockRegistry().addRule("wheat", "beetroot", FROST_HARDINESS.getId(), 0.1);

        // Demo hard incompatibility: cactus (bone-dry sand specialist) and nether wart (Nether
        // soul-sand specialist) never form a cross seed at all, regardless of stats or luck.
        AgriApi.getAttributeUnlockRegistry().markIncompatible("cactus", "nether_wart");

        AgriCore.getLogger("agricrops").info("Registered fork-native species and demo attribute rules.");
    }

    private static void registerPlainSpecies(String plantId) {
        final CropSpecies.Builder builder = CropSpecies.builder(plantId);
        if ("wheat".equals(plantId) || "beetroot".equals(plantId)) {
            builder.lockedAttributeSlot(FROST_HARDINESS);
        }
        AgriApi.getSpeciesRegistry().add(builder.build());
    }

}
