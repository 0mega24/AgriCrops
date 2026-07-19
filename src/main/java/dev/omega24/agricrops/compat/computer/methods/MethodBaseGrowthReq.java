package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import static dev.omega24.agricrops.compat.computer.methods.MethodUtilities.*;
import dev.omega24.agricrops.tiles.TileEntityPeripheral;
import dev.omega24.agricrops.tiles.TileEntityCrop;
import java.util.ArrayList;
import java.util.Optional;

public abstract class MethodBaseGrowthReq extends MethodBase {

    public MethodBaseGrowthReq(String name) {
        super(name, true, true, true);
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) throws InvocationException {
        return onMethodCalled(getCropPlant(crop));
    }

    @Override
    protected Object[] onMethodCalled(TileEntityPeripheral peripheral) throws InvocationException {
        return onMethodCalled(getCropPlant(peripheral.getSpecimen()));
    }

    protected abstract Object[] onMethodCalled(Optional<IAgriPlant> plant) throws InvocationException;

    @Override
    protected ArrayList<MethodParameter> getParameters() {
        ArrayList<MethodParameter> pars = new ArrayList<>();
        pars.add(MethodParameter.DIRECTION_OPTIONAL);
        return pars;
    }
}
