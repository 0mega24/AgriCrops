/*
 */
package dev.omega24.agricrops.api.v1.soil;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

/**
 * Maps a {@link Block} to its {@link ISoilProfile}, per crop-mod-design.md §3. Matching is
 * by block identity only (metadata/variant-agnostic) — unlike the old item-based soil
 * registry, blocks don't need an item form to be registered here (this is what let
 * farmland be a soil in the first place). An unregistered block has no profile and is
 * unsupported: callers must not silently fall back to a generic profile.
 */
public interface ISoilProfileRegistry {

    /**
     * Registers a profile for the given block. Returns {@literal false} without registering
     * if the block already has a profile.
     */
    boolean register(@Nonnull Block block, @Nonnull ISoilProfile profile);

    @Nonnull
    Optional<ISoilProfile> get(@Nullable Block block);

    @Nonnull
    Optional<ISoilProfile> get(@Nullable IBlockState state);

    /**
     * All blocks with a registered profile. Mainly useful for displaying a representative
     * block for a composition tag (e.g. in JEI), rather than for gameplay logic.
     */
    @Nonnull
    Collection<Block> getRegisteredBlocks();

}
