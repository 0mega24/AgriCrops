package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import java.util.Optional;
import net.minecraftforge.common.BiomeDictionary;

/**
 * crop-mod-design.md §9: {@code getIdealConditions()} - one aggregate table combining
 * preferred biome, elevation band, soil tags, and light range, for scripts that want a full
 * placement checklist rather than several separate lookups.
 */
public class MethodGetIdealConditions extends MethodBaseGrowthReq {

    public MethodGetIdealConditions() {
        super("getIdealConditions");
    }

    @Override
    protected Object[] onMethodCalled(Optional<IAgriPlant> plant) {
        if (!plant.isPresent()) {
            return new Object[]{null};
        }
        final IAgriPlant p = plant.get();
        final Object[] biomes = p.getPreferredBiomeTypes().stream()
                .map(BiomeDictionary.Type::getName)
                .toArray();
        final Object[] soils = p.getGrowthRequirement().getSoilCompositions().stream()
                .map(s -> s.getName())
                .toArray();
        return new Object[]{
            biomes,
            p.getOptimalElevationMin(),
            p.getOptimalElevationMax(),
            soils,
            p.getGrowthRequirement().getMinLight(),
            p.getGrowthRequirement().getMaxLight()
        };
    }
}
