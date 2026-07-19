package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.tiles.TileEntityCrop;

public class MethodIsFertile extends MethodBaseCrop {

    public MethodIsFertile() {
        super("isFertile");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) {
        return new Object[]{crop.isFertile()};
    }

}
