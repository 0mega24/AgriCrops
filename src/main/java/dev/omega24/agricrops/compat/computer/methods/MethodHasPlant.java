package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.tiles.TileEntityCrop;

public class MethodHasPlant extends MethodBaseCrop {

    public MethodHasPlant() {
        super("hasPlant");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) {
        return new Object[]{crop.hasSeed()};
    }
}
