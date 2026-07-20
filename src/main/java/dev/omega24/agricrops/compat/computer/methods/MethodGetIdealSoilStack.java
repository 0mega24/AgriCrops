package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import java.util.Optional;
import net.minecraft.block.Block;

/**
 * crop-mod-design.md §9: {@code getIdealSoilStack()} - a concrete block that satisfies the
 * species' top soil preference (e.g. sand, if sand is preferred), so a fetch/logistics robot
 * can go shopping for the right block instead of parsing a tag name back into an item itself.
 * Picks the first registered {@code ISoilProfile} block whose composition matches; which
 * specific block wins among several sharing a composition is unspecified.
 */
public class MethodGetIdealSoilStack extends MethodBaseGrowthReq {

    public MethodGetIdealSoilStack() {
        super("getIdealSoilStack");
    }

    @Override
    protected Object[] onMethodCalled(Optional<IAgriPlant> plant) {
        final Optional<ISoilComposition> preferred = plant
                .flatMap(p -> p.getGrowthRequirement().getSoilCompositions().stream().findFirst());
        if (!preferred.isPresent()) {
            return new Object[]{"null"};
        }
        for (Block block : AgriApi.getSoilProfileRegistry().getRegisteredBlocks()) {
            final boolean matches = AgriApi.getSoilProfileRegistry().get(block)
                    .map(profile -> profile.getComposition().equals(preferred.get()))
                    .orElse(false);
            if (matches) {
                return new Object[]{Block.REGISTRY.getNameForObject(block).toString()};
            }
        }
        return new Object[]{"null"};
    }
}
