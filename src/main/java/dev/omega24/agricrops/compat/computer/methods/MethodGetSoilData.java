package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.farming.soil.SoilColumnHelper;
import dev.omega24.agricrops.tiles.TileEntityCrop;

/**
 * crop-mod-design.md §9: {@code getSoilData()} - depth, composition, nutrient pool, and
 * moisture for the soil column below this crop stick. Read-only: unlike the internal growth
 * tick logic this never creates a column as a side effect of being queried.
 */
public class MethodGetSoilData extends MethodBaseCrop {

    public MethodGetSoilData() {
        super("getSoilData");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) {
        return SoilColumnHelper.getColumn(crop.getWorld(), crop.getPos().down())
                .map(column -> new Object[]{
                    column.getDepth(),
                    column.getComposition().getName(),
                    column.getNutrients(),
                    column.getNutrientCapacity(),
                    column.getMoisture()
                })
                .orElse(new Object[]{0, "none", 0, 0, 0});
    }
}
