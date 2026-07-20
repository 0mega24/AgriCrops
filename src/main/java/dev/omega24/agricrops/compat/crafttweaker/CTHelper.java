package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import javax.annotation.Nonnull;

/**
 * Small shared plumbing for the {@code mods.agricrops.*} ZenScript bridge (crop-mod-design.md
 * §7/§11's "mostly via CraftTweaker" note on {@link dev.omega24.agricrops.api.v1.attribute.LockedAttribute}).
 * <p>
 * Deliberately NOT wrapped in CraftTweaker's {@code IAction}/action-queue pattern (unlike its
 * recipe add/remove calls): that queue exists to defer Forge-recipe-registry interactions
 * until after all scripts finish parsing, which matters for recipe load-order conflicts
 * between mods. Agricrops' registries (soil compositions, species, locked attributes,
 * mutations) have no such conflict to defer around, and deferring them anyway would silently
 * break cross-file ordering: a plant registration that resolves a soil composition by id needs
 * that composition to already be in the registry, which only holds if earlier calls ran
 * synchronously rather than queuing to a later shared apply-phase. Executing immediately, in
 * the order a pack dev's scripts actually run, is both simpler and safer here.
 */
final class CTHelper {

    private CTHelper() {
    }

    /**
     * Runs {@code action} immediately, logging {@code description} to CraftTweaker's log first
     * so a pack dev can see exactly which registrations ran and in what order.
     */
    static void apply(@Nonnull String description, @Nonnull Runnable action) {
        CraftTweakerAPI.logInfo(description);
        action.run();
    }

}
