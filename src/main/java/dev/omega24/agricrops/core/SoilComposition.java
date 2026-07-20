/*
 */
package dev.omega24.agricrops.core;

import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import javax.annotation.Nonnull;

/**
 * A simple, code-registered {@link ISoilComposition} tag.
 */
public class SoilComposition implements ISoilComposition {

    @Nonnull
    private final String id;
    @Nonnull
    private final String name;

    public SoilComposition(@Nonnull String id, @Nonnull String name) {
        this.id = id;
        this.name = name;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

}
