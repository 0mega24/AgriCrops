package dev.omega24.agricrops.blocks.properties;

import dev.omega24.agricrops.utility.CustomWoodType;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyCustomWood implements IUnlistedProperty<CustomWoodType> {

    private final String name;

    public PropertyCustomWood(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(CustomWoodType value) {
        return true;
    }

    @Override
    public Class<CustomWoodType> getType() {
        return CustomWoodType.class;
    }

    @Override
    public String valueToString(CustomWoodType value) {
        return value.getBlock().getRegistryName().toString() + ":" + value.getMeta();
    }
}
