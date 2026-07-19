package dev.omega24.agricrops.world;

import dev.omega24.agricrops.handler.VillageCreationHandler;
import dev.omega24.agricrops.reference.AgriCraftConfig;
import dev.omega24.agricrops.reference.Reference;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public class WorldGen {
    public static void init()
    {
        if (!AgriCraftConfig.disableWorldGen) {
            MapGenStructureIO.registerStructureComponent(StructureGreenHouse.class, Reference.MOD_ID + ":Greenhouse");
            VillagerRegistry.instance().registerVillageCreationHandler(new VillageCreationHandler.GreenHouseHandler());

        }
    }
}
