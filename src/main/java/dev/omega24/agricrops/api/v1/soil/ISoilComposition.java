/*
 */
package dev.omega24.agricrops.api.v1.soil;

import dev.omega24.agricrops.api.v1.misc.IAgriRegisterable;
import javax.annotation.Nonnull;

/**
 * A registered soil composition tag (dirt, tilled, sand, clay, stone, ...), per
 * crop-mod-design.md §3. Open registry: mods/packs can register their own tags via
 * {@link dev.omega24.agricrops.api.v1.AgriApi#getSoilCompositionRegistry()} the same way
 * plants and mutations are registered.
 */
public interface ISoilComposition extends IAgriRegisterable {

    @Nonnull
    @Override
    String getId();

    @Nonnull
    String getName();

}
