package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.tiles.TileEntityPeripheral;
import java.util.ArrayList;

public class MethodAnalyze extends MethodBasePeripheral {

    public MethodAnalyze() {
        super("analyze");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityPeripheral peripheral) throws InvocationException {
        peripheral.startAnalyzing();
        return new Object[0];
    }

    @Override
    protected ArrayList<MethodParameter> getParameters() {
        return new ArrayList<>();
    }

}
