/*
 */
package dev.omega24.agricrops.farming.soil;

import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

public class ChunkSoilColumns implements IChunkSoilColumns {

    private final Map<BlockPos, ISoilColumn> columns = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public Optional<ISoilColumn> get(@Nullable BlockPos pos) {
        return Optional.ofNullable(pos).map(this.columns::get);
    }

    @Nonnull
    @Override
    public ISoilColumn getOrCreate(@Nonnull BlockPos pos, @Nonnull ISoilProfile profile) {
        return this.columns.computeIfAbsent(pos.toImmutable(), p -> new SoilColumn(profile));
    }

    @Override
    public void remove(@Nonnull BlockPos pos) {
        this.columns.remove(pos);
    }

    @Nonnull
    Map<BlockPos, ISoilColumn> getColumns() {
        return this.columns;
    }

}
