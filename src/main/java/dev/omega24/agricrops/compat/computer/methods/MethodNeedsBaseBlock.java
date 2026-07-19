package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import java.util.Optional;

public class MethodNeedsBaseBlock extends MethodBaseGrowthReq {

    public MethodNeedsBaseBlock() {
        super("needsBaseBlock");
    }

    @Override
    protected Object[] onMethodCalled(Optional<IAgriPlant> plant) throws InvocationException {
        return new Object[]{plant.flatMap(p -> p.getGrowthRequirement().getConditionStack()).isPresent()};
    }

}
