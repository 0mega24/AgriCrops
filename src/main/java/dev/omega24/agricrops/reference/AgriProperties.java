package dev.omega24.agricrops.reference;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.blocks.properties.PropertyCropPlant;
import dev.omega24.agricrops.blocks.properties.PropertyCustomWood;
import dev.omega24.agricrops.blocks.properties.UnlistedPropertyBoolean;
import dev.omega24.agricrops.blocks.properties.UnlistedPropertyInteger;
import dev.omega24.agricrops.utility.CustomWoodType;
import com.infinityraider.infinitylib.block.blockstate.InfinityProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

/**
 * A class containing AgriCraft block state properties
 */
public interface AgriProperties {

    InfinityProperty<Boolean> JOURNAL = new InfinityProperty<>(PropertyBool.create("journal"), true);
    InfinityProperty<EnumFacing> FACING = new InfinityProperty<>(PropertyDirection.create("facing"), EnumFacing.NORTH);

    IUnlistedProperty<IAgriPlant> CROP_PLANT = PropertyCropPlant.PROPERTY;
    IUnlistedProperty<Integer> GROWTH_STAGE = new UnlistedPropertyInteger("agri_growth_stage");
    IUnlistedProperty<Boolean> CROSS_CROP = new UnlistedPropertyBoolean("agri_cross_crop");

    InfinityProperty<Boolean> POWERED = new InfinityProperty<>(PropertyBool.create("powered"), false);

    IUnlistedProperty<CustomWoodType> CUSTOM_WOOD_TYPE = new PropertyCustomWood("wood_type");

}
