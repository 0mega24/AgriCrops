/*
 */
package dev.omega24.agricrops.farming.soil;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import dev.omega24.agricrops.handler.SoilColumnCapabilityHandler;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Entry point for reading/creating a world position's {@link ISoilColumn}. Callers should
 * use this instead of touching the chunk capability directly.
 */
public final class SoilColumnHelper {

    private SoilColumnHelper() {
    }

    /**
     * Fetches the column at {@code pos}, if one has already been created there.
     */
    @Nonnull
    public static Optional<ISoilColumn> getColumn(@Nonnull World world, @Nonnull BlockPos pos) {
        return getChunkColumns(world, pos).flatMap(columns -> columns.get(pos));
    }

    /**
     * Fetches the column at {@code pos}, creating one if the block there has a registered
     * {@link ISoilProfile}. Empty if the block is unsupported (no profile registered) —
     * per design §3, this stays an explicit "doesn't work yet" rather than a silent
     * generic fallback.
     */
    @Nonnull
    public static Optional<ISoilColumn> getOrCreateColumn(@Nonnull World world, @Nonnull BlockPos pos) {
        final Optional<ISoilProfile> profile = AgriApi.getSoilProfileRegistry().get(world.getBlockState(pos));
        if (!profile.isPresent()) {
            return Optional.empty();
        }
        return getChunkColumns(world, pos).map(columns -> columns.getOrCreate(pos, profile.get()));
    }

    /**
     * How far (horizontally) to scan for an open water source that can top up a soil
     * column's moisture, same radius vanilla farmland uses for its own moisture check
     * (crop-mod-design.md §4: "Water sources, sprinklers, or OC-driven irrigation top it
     * up.").
     */
    private static final int WATER_SEARCH_RADIUS = 4;

    /**
     * Whether an open water source block is within range of {@code soilPos} — same
     * horizontal-box scan vanilla {@code BlockFarmland} uses, checked at the soil block's
     * own layer and the layer above it (where the crop stick sits).
     */
    public static boolean hasNearbyWaterSource(@Nonnull World world, @Nonnull BlockPos soilPos) {
        for (int x = -WATER_SEARCH_RADIUS; x <= WATER_SEARCH_RADIUS; x++) {
            for (int z = -WATER_SEARCH_RADIUS; z <= WATER_SEARCH_RADIUS; z++) {
                for (int y = 0; y <= 1; y++) {
                    final BlockPos check = soilPos.add(x, y, z);
                    if (world.getBlockState(check).getMaterial() == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nonnull
    private static Optional<IChunkSoilColumns> getChunkColumns(@Nonnull World world, @Nonnull BlockPos pos) {
        final Chunk chunk = world.getChunkFromBlockCoords(pos);
        return Optional.ofNullable(chunk.getCapability(SoilColumnCapabilityHandler.SOIL_COLUMNS_CAPABILITY, null));
    }

}
