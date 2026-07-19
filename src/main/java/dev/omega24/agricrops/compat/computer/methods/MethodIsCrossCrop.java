package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.tiles.TileEntityCrop;

public class MethodIsCrossCrop extends MethodBaseCrop {

    public MethodIsCrossCrop() {
        super("isCrossCrop");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) {
        return new Object[]{crop.isCrossCrop()};
    }

}
