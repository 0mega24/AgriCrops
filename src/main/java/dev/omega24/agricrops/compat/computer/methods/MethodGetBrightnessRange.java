package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import java.util.Optional;

public class MethodGetBrightnessRange extends MethodBaseGrowthReq {

    public MethodGetBrightnessRange() {
        super("getBrightnessRange");
    }

    @Override
    protected Object[] onMethodCalled(Optional<IAgriPlant> plant) {
        return plant
                .map(IAgriPlant::getGrowthRequirement)
                .map(g -> new Object[]{g.getMinLight(), g.getMaxLight()})
                .orElse(null);
    }
}
