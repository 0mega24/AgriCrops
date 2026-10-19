package dev.omega24.agricrops.core;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.AgriApi;

/** Built-in breeding progression that is available without JSON or CraftTweaker. */
public final class DefaultMutations {

    public static final String WINTER_WHEAT_MUTATION = "wheat_beetroot_to_winter_wheat";

    private DefaultMutations() {
    }

    public static void register() {
        if (!AgriApi.getMutationRegistry().add(
                WINTER_WHEAT_MUTATION, 0.35, "winter_wheat", "wheat", "beetroot")) {
            AgriCore.getLogger("agricrops").error(
                    "Failed to register built-in mutation {0}.", WINTER_WHEAT_MUTATION);
            return;
        }
        AgriCore.getLogger("agricrops").info(
                "Registered built-in mutation: {0}.", WINTER_WHEAT_MUTATION);
    }
}
