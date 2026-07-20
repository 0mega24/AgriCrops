/*
 */
package dev.omega24.agricrops.core;

import dev.omega24.agricrops.api.v1.render.RenderMethod;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import net.minecraft.util.ResourceLocation;

/**
 * The full-conversion weed a destabilizing plant becomes if its telegraph window
 * (crop-mod-design.md §8) expires uncaught. Aggressive and fast-growing so it reaches
 * maturity and starts {@link dev.omega24.agricrops.tiles.TileEntityCrop#spread()} quickly, and
 * yields nothing on harvest — {@link #isWeed()} makes it uproot-only (rake required, per
 * {@code IAgriPlant#isWeed()}'s own contract), and it drops no seeds at all, so clearing an
 * established weed is strictly worse than catching it during the destabilizing window, per §8's
 * requirement.
 *
 * <p>Reuses the leftover {@code weed_stage_0..3} art already vendored under
 * {@code assets/agricrops} (never used by any upstream AgriCraft default plant) rather than
 * adding new textures.
 */
public class WeedPlant extends CodePlant {

    public static final String ID = "weed";

    public WeedPlant() {
        super(ID, "Weed", Arrays.asList("dirt", "grass", "tilled", "sand", "gravel", "clay", "stone", "soul_sand"),
                4, RenderMethod.CROSSED,
                stage -> new ResourceLocation("agricrops:blocks/weed_stage_" + stage),
                new ResourceLocation("agricrops:items/seed_silverweed"),
                Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public boolean isWeed() {
        return true;
    }

    @Override
    public boolean isAggressive() {
        return true;
    }

    @Override
    public boolean isFertilizable() {
        // A weed doesn't need help; also keeps fertilizer from being wasted "curing" it faster.
        return false;
    }

    @Override
    public double getSpreadChance() {
        return 0.1;
    }

    @Override
    public double getGrowthChanceBase() {
        return 0.15;
    }

    @Override
    public double getGrowthChanceBonus() {
        return 0.05;
    }

    @Override
    public double getSeedDropChanceBase() {
        return 0.0;
    }

    @Override
    public double getSeedDropChanceBonus() {
        return 0.0;
    }

    @Nonnull
    @Override
    public String getInformation() {
        return "A weed. It spreads to neighboring crop sticks and yields nothing - uproot it with a rake.";
    }

}
