package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.tiles.TileEntityCrop;

/**
 * crop-mod-design.md §9: {@code getGrowthProgress()} - current stage / stage count, plus
 * which loot tier (§6) the current stage qualifies for (-1 if none yet) - handy for a
 * harvesting robot deciding whether to wait.
 */
public class MethodGetGrowthProgress extends MethodBaseCrop {

    public MethodGetGrowthProgress() {
        super("getGrowthProgress");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) {
        if (!crop.hasSeed()) {
            return new Object[]{0, 0, -1};
        }
        final int stage = crop.getGrowthStage();
        final int stages = crop.getSeed().getPlant().getGrowthStages();
        final int lootTierStage = crop.getCurrentLootTierMinStage().orElse(-1);
        return new Object[]{stage, stages, lootTierStage};
    }
}
