package dev.omega24.agricrops.farming;

import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlantStatsTest {

    @Test
    public void valuesClampAndRoundTripThroughNbt() {
        IAgriStat original = new PlantStats(true)
                .with(CropStat.GROWTH, CropStat.MAX + 20)
                .with(CropStat.LIGHT_TOLERANCE, CropStat.MIN - 20)
                .with(CropStat.YIELD, 17);
        NBTTagCompound tag = new NBTTagCompound();

        assertTrue(original.writeToNBT(tag));
        IAgriStat restored = new PlantStats().valueOf(tag).get();

        assertEquals(CropStat.MAX, restored.get(CropStat.GROWTH));
        assertEquals(CropStat.MIN, restored.get(CropStat.LIGHT_TOLERANCE));
        assertEquals(17, restored.get(CropStat.YIELD));
        assertTrue(restored.isAnalyzed());
        assertEquals(original, restored);
    }
}
