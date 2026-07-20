package dev.omega24.agricrops.api.v1.plant;

import dev.omega24.agricrops.api.v1.crop.IAgriCrop;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

/**
 * One entry in a plant's staged loot list (crop-mod-design.md §6): the growth stage a
 * harvest must reach to qualify for this tier, the products it rolls, and whether reaching
 * it resets the plant's growth stage back to zero (only a max-stage "full" tier normally
 * should; lower tiers are meant to be repeat-harvestable, IC2-Crops style).
 */
public final class CropLootTier {

    public interface LootRoller {
        void roll(@Nonnull Consumer<ItemStack> products, @Nonnull IAgriCrop crop, @Nonnull IAgriStat stat, @Nonnull Random rand);
    }

    private final int minStage;
    private final LootRoller roller;
    private final boolean resetsGrowth;

    public CropLootTier(int minStage, @Nonnull LootRoller roller, boolean resetsGrowth) {
        this.minStage = minStage;
        this.roller = Objects.requireNonNull(roller);
        this.resetsGrowth = resetsGrowth;
    }

    public int getMinStage() {
        return this.minStage;
    }

    public boolean resetsGrowth() {
        return this.resetsGrowth;
    }

    public void roll(@Nonnull Consumer<ItemStack> products, @Nonnull IAgriCrop crop, @Nonnull IAgriStat stat, @Nonnull Random rand) {
        this.roller.roll(products, crop, stat, rand);
    }

}
