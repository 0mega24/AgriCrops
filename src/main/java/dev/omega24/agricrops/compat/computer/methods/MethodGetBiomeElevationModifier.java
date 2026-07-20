package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.tiles.TileEntityCrop;

/**
 * crop-mod-design.md §9: {@code getBiomeElevationModifier()} - the crop's current
 * biome/elevation habitat satisfaction (0..1), for automation/tuning.
 */
public class MethodGetBiomeElevationModifier extends MethodBaseCrop {

    public MethodGetBiomeElevationModifier() {
        super("getBiomeElevationModifier");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) {
        return new Object[]{crop.getBiomeElevationModifier()};
    }
}
