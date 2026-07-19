package dev.omega24.agricrops.blocks.properties;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyCropPlant implements IUnlistedProperty<IAgriPlant> {

    public static final PropertyCropPlant PROPERTY = new PropertyCropPlant();

    private PropertyCropPlant() {
    }

    @Override
    public String getName() {
        return "crop_plant";
    }

    @Override
    public boolean isValid(IAgriPlant value) {
        return true;
    }

    @Override
    public Class<IAgriPlant> getType() {
        return IAgriPlant.class;
    }

    @Override
    public String valueToString(IAgriPlant value) {
        return value.getId();
    }
}
