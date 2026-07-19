package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.tiles.TileEntityCrop;

public class MethodIsMature extends MethodBaseCrop {

    public MethodIsMature() {
        super("isMature");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) {
        return new Object[]{crop.isMature()};
    }

}
