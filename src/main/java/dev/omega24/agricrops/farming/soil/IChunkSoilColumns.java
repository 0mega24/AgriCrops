/*
 */
package dev.omega24.agricrops.farming.soil;

import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

/**
 * The actual capability payload attached to each {@link net.minecraft.world.chunk.Chunk}:
 * every {@link ISoilColumn} in that chunk, keyed by world position. Forge capabilities
 * can't attach directly to a bare {@link net.minecraft.block.Block} (it's a singleton
 * shared by every occurrence of that block), so per-column data lives here instead,
 * created lazily only when a column is first touched (crop-mod-design.md §3: "per-column,
 * not per-TileEntity").
 */
public interface IChunkSoilColumns {

    @Nonnull
    Optional<ISoilColumn> get(@Nullable BlockPos pos);

    /**
     * Fetches the column at {@code pos}, creating one from {@code profile} if none exists yet.
     */
    @Nonnull
    ISoilColumn getOrCreate(@Nonnull BlockPos pos, @Nonnull ISoilProfile profile);

    void remove(@Nonnull BlockPos pos);

}
