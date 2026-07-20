/*
 */
package dev.omega24.agricrops.impl.v1;

import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import dev.omega24.agricrops.api.v1.soil.ISoilProfileRegistry;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class SoilProfileRegistry implements ISoilProfileRegistry {

    private final ConcurrentMap<Block, ISoilProfile> registry = new ConcurrentHashMap<>();

    @Override
    public boolean register(@Nonnull Block block, @Nonnull ISoilProfile profile) {
        return this.registry.putIfAbsent(block, profile) == null;
    }

    @Nonnull
    @Override
    public Optional<ISoilProfile> get(@Nullable Block block) {
        return Optional.ofNullable(block).map(this.registry::get);
    }

    @Nonnull
    @Override
    public Optional<ISoilProfile> get(@Nullable IBlockState state) {
        return Optional.ofNullable(state).map(IBlockState::getBlock).flatMap(this::get);
    }

    @Nonnull
    @Override
    public Collection<Block> getRegisteredBlocks() {
        return Collections.unmodifiableSet(this.registry.keySet());
    }

}
