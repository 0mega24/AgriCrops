/*
 */
package dev.omega24.agricrops.impl.v1;

import dev.omega24.agricrops.api.v1.soil.IAgriSoil;
import dev.omega24.agricrops.api.v1.soil.IAgriSoilRegistry;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Ryan
 */
public class AgriSoilRegistry extends AgriRegistry<IAgriSoil> implements IAgriSoilRegistry {

    @Override
    public boolean contains(@Nullable IBlockState state) {
        // Block-identity soils first (itemless blocks like farmland that can't be FuzzyStacks).
        if (state != null && this.stream().anyMatch(s -> s.matchesBlock(state.getBlock()))) {
            return true;
        }
        return FuzzyStack.from(state)
                .filter(this::contains)
                .isPresent();
    }

    @Override
    public boolean contains(@Nullable ItemStack stack) {
        return FuzzyStack.from(stack)
                .filter(this::contains)
                .isPresent();
    }

    @Override
    public boolean contains(@Nullable FuzzyStack stack) {
        return (stack != null) && (this.stream().anyMatch(e -> e.isVarient(stack)));
    }

    @Nonnull
    @Override
    public Optional<IAgriSoil> get(@Nullable IBlockState state) {
        if (state != null) {
            final Block block = state.getBlock();
            final Optional<IAgriSoil> byBlock = this.stream().filter(s -> s.matchesBlock(block)).findFirst();
            if (byBlock.isPresent()) {
                return byBlock;
            }
        }
        return FuzzyStack.from(state).flatMap(this::get);
    }

    @Nonnull
    @Override
    public Optional<IAgriSoil> get(@Nullable ItemStack stack) {
        return FuzzyStack.from(stack).flatMap(this::get);
    }

    @Nonnull
    @Override
    public Optional<IAgriSoil> get(@Nullable FuzzyStack stack) {
        if (stack != null) {
            return this.stream().filter(e -> e.isVarient(stack)).findFirst();
        } else {
            return Optional.empty();
        }
    }

}
