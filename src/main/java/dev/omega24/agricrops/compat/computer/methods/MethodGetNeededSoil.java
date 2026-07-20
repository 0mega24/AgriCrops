package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import java.util.Optional;

public class MethodGetNeededSoil extends MethodBaseGrowthReq {

    public MethodGetNeededSoil() {
        super("getNeededSoil");
    }

    @Override
    protected Object[] onMethodCalled(Optional<IAgriPlant> plant) {
        return new Object[]{plant.flatMap(this::getMainSoil).orElse("null")};
    }

    private Optional<String> getMainSoil(IAgriPlant plant) {
        return plant.getGrowthRequirement().getSoilCompositions().stream()
                .findFirst()
                .map(s -> s.getName());
    }
}
