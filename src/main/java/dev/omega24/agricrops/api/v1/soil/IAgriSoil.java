/*
 */
package dev.omega24.agricrops.api.v1.soil;

import dev.omega24.agricrops.api.v1.misc.IAgriRegisterable;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import java.util.Collection;
import javax.annotation.Nonnull;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

/**
 * Class for interacting with AgriCraft soil definitions.
 */
public interface IAgriSoil extends IAgriRegisterable {

    @Nonnull
    @Override
    String getId();

    @Nonnull
    String getName();

    /**
     * Returns an ItemStack representative of this AgriSoil.
     *
     * @return an ItemStack representing this soil.
     */
    @Nonnull
    Collection<FuzzyStack> getVarients();

    default boolean isVarient(@Nonnull ItemStack stack) {
        // Validate
        Preconditions.checkNotNull(stack);

        // Delegate
        return isVarient(new FuzzyStack(stack));
    }

    default boolean isVarient(@Nonnull FuzzyStack stack) {
        // Validate
        Preconditions.checkNotNull(stack);

        // Evaluate
        return this.getVarients().contains(stack);
    }

    /**
     * Whether this soil matches the given block directly, by block identity. This exists
     * for blocks with no item form (e.g. farmland) that cannot be represented as a
     * {@link FuzzyStack}. Item-backed soils match via {@link #getVarients()} instead.
     *
     * @param block the block below a crop.
     * @return true if this soil accepts that block regardless of its item form.
     */
    default boolean matchesBlock(@Nonnull Block block) {
        return false;
    }

}
