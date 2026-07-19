package dev.omega24.agricrops.utility;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.farming.PlantStats;
import java.util.List;
import java.util.Random;

/**
 * A helper class for working with generation.
 *
 *
 */
public class WorldGenerationHelper {

    /**
     * Gets a random SEED from a list of plants
     *
     * @param rand Random object to be used
     * @param setTag If the SEED should be initialized with an AgriCraftNBT TAG containing random
     * stats
     * @param plants List of plants to grab a random SEED from
     * @return an ItemStack containing a random SEED
     */
    public static AgriSeed getRandomSeed(Random rand, boolean setTag, List<IAgriPlant> plants) {
        return new AgriSeed(
                plants.get(rand.nextInt(plants.size())),
                getRandomStat(rand)
        );
    }

    public static IAgriStat getRandomStat(Random rand) {
        IAgriStat stat = new PlantStats();
        for (CropStat cropStat : CropStat.values()) {
            stat = stat.with(cropStat, getRandomStatValue(rand));
        }
        return stat;
    }

    public static int getRandomStatValue(Random rand) {
        return rand.nextInt(CropStat.MAX) / 2 + 1;
    }

}
