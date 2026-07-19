/*
 */
package dev.omega24.agricrops.core;

import dev.omega24.agricrops.api.v1.soil.IAgriSoil;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * An {@link IAgriSoil} defined directly in code (an Agricrops fork-native soil), as
 * opposed to {@link JsonSoil} which wraps an AgriCore JSON definition.
 */
public class CodeSoil implements IAgriSoil {

    @Nonnull
    private final String id;
    @Nonnull
    private final String name;
    @Nonnull
    private final List<FuzzyStack> variants;

    public CodeSoil(@Nonnull String id, @Nonnull String name, @Nonnull List<FuzzyStack> variants) {
        this.id = id;
        this.name = name;
        this.variants = variants;
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

    @Nonnull
    @Override
    public Collection<FuzzyStack> getVarients() {
        return Collections.unmodifiableList(this.variants);
    }

}
