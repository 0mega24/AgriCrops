/*
 */
package dev.omega24.agricrops.handler;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.reference.AgriCraftConfig;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 *
 * @author Ryan
 */
@Mod.EventBusSubscriber(modid = "agricrops")
public final class GrassDropHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interceptGrassDrop(BlockEvent.HarvestDropsEvent event) {

        // Skip silk touch.
        if (event.isSilkTouching()) {
            return;
        }

        // Fetch the blockstate.
        final IBlockState state = event.getState();

        // Skip Air or Error
        if (state == null || state.getBlock() == null) {
            return;
        }

        // Fetch the world random.
        final Random rand = event.getWorld().rand;

        // Log
        // This line was oddly ignoring the debug settings...
        //AgriCore.getLogger("agricrops").debug("Intercepted! Block: {0}", state.getBlock());
        // Add grass drops if grass block.
        if (state.getBlock() instanceof BlockTallGrass) {
            // Wipe other drops, if needed.
            if (AgriCraftConfig.wipeGrassDrops) {
                event.getDrops().clear();
            }
            // Log
            // Commenented out to prevent spam.
            //AgriCore.getLogger("agricrops").debug("Inserting Drops!");
            // Add the drops.
            addGrassDrops(event.getDrops(), rand);
        }
    }

    public static void addGrassDrops(List<ItemStack> drops, Random rand) {
        AgriApi.getPlantRegistry()
                .all()
                .stream()
                .forEach(p -> addGrassDrop(drops, rand, p));
    }

    public static void addGrassDrop(List<ItemStack> drops, Random rand, IAgriPlant plant) {
        if (plant.getGrassDropChance() > rand.nextDouble()) {
            drops.add(plant.getSeed());
        }
    }

}
