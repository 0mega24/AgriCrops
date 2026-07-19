package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.tiles.TileEntityPeripheral;
import dev.omega24.agricrops.tiles.TileEntityCrop;
import java.util.ArrayList;
import java.util.Optional;

public class MethodIsAnalyzed extends MethodBase {

    public MethodIsAnalyzed() {
        super("isAnalyzed", false, true, true);
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) throws InvocationException {
        return new Object[]{Optional.ofNullable(crop.getSeed()).map(s -> s.getStat()).filter(IAgriStat::isAnalyzed).isPresent()};
    }

    @Override
    protected Object[] onMethodCalled(TileEntityPeripheral peripheral) throws InvocationException {
        return new Object[]{peripheral.isSpecimenAnalyzed()};
    }

    @Override
    protected ArrayList<MethodParameter> getParameters() {
        return null;
    }
}
