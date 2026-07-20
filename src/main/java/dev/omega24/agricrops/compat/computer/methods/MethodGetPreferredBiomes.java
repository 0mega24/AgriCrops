package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import java.util.Optional;
import net.minecraftforge.common.BiomeDictionary;

/**
 * crop-mod-design.md §9: {@code getPreferredBiomes()} - the species' preferred biome
 * dictionary tags (§5), as readable names rather than just a modifier number.
 */
public class MethodGetPreferredBiomes extends MethodBaseGrowthReq {

    public MethodGetPreferredBiomes() {
        super("getPreferredBiomes");
    }

    @Override
    protected Object[] onMethodCalled(Optional<IAgriPlant> plant) {
        return plant
                .map(p -> p.getPreferredBiomeTypes().stream()
                        .map(BiomeDictionary.Type::getName)
                        .toArray())
                .orElse(new Object[0]);
    }
}
