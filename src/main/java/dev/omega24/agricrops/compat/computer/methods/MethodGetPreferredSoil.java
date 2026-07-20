package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import java.util.Optional;

/**
 * crop-mod-design.md §9: {@code getPreferredSoil()} - the composition tags (§3) this species
 * prefers or requires, as a readable list (e.g. {@code ["sand", "clay"]}), rather than a
 * script inferring soil preference by trial planting. Distinct from the legacy singular
 * {@link MethodGetNeededSoil}, which only ever reported the first composition.
 */
public class MethodGetPreferredSoil extends MethodBaseGrowthReq {

    public MethodGetPreferredSoil() {
        super("getPreferredSoil");
    }

    @Override
    protected Object[] onMethodCalled(Optional<IAgriPlant> plant) {
        return plant
                .map(p -> p.getGrowthRequirement().getSoilCompositions().stream()
                        .map(s -> s.getName())
                        .toArray())
                .orElse(new Object[0]);
    }
}
