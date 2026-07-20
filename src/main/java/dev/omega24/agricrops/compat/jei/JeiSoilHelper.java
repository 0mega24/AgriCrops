/*
 */
package dev.omega24.agricrops.compat.jei;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.requirement.IGrowthRequirement;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Representative {@link ItemStack}s for a growth requirement's soil compositions, for JEI
 * display only. There's no longer a direct soil-tag-to-ItemStack mapping (design §3's
 * {@code ISoilProfile} registry is keyed by {@link Block}, not by composition tag), so this
 * looks up one registered block per required composition instead.
 */
public final class JeiSoilHelper {

    private JeiSoilHelper() {
    }

    /**
     * One representative stack per soil composition required by {@code requirement}, or a
     * single farmland stack if the requirement accepts any registered soil.
     */
    @Nonnull
    public static List<ItemStack> representativeStacks(@Nonnull IGrowthRequirement requirement) {
        final List<ItemStack> stacks = new ArrayList<>();
        for (ISoilComposition composition : requirement.getSoilCompositions()) {
            representativeStack(composition).ifPresent(stacks::add);
        }
        if (stacks.isEmpty()) {
            stacks.add(new ItemStack(Blocks.FARMLAND));
        }
        return stacks;
    }

    @Nonnull
    private static java.util.Optional<ItemStack> representativeStack(@Nonnull ISoilComposition composition) {
        for (Block block : AgriApi.getSoilProfileRegistry().getRegisteredBlocks()) {
            final boolean matches = AgriApi.getSoilProfileRegistry().get(block)
                    .filter(p -> p.getComposition().equals(composition))
                    .isPresent();
            if (!matches) {
                continue;
            }
            final Item item = Item.getItemFromBlock(block);
            if (item != Items.AIR) {
                return java.util.Optional.of(new ItemStack(block));
            }
        }
        return java.util.Optional.empty();
    }

}
