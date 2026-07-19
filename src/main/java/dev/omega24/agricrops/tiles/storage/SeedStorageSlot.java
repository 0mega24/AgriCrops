package dev.omega24.agricrops.tiles.storage;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.reference.AgriNBT;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SeedStorageSlot {

    private AgriSeed seed;
    public int count;
    private int slotId;
    private int invId;

    public SeedStorageSlot(AgriSeed seed, int count, int slotId, int invId) {
        this.seed = seed;
        this.count = count;
        this.slotId = slotId;
        this.invId = invId;
    }

    public ItemStack toStack() {
        return seed.toStack(count);
    }

    public AgriSeed getSeed() {
        return seed;
    }

    private int getTotalStat() {
        int total = 0;
        for (CropStat stat : CropStat.values()) {
            total += this.seed.getStat().get(stat);
        }
        return total;
    }

    public int getId() {
        return invId >= 0 ? (1000 * invId) + slotId : slotId;
    }

    public void writeToNbt(NBTTagCompound tag) {
        this.seed.toStack().writeToNBT(tag);
        tag.setInteger(AgriNBT.COUNT, this.count);
        tag.setInteger(AgriNBT.ID, this.slotId);
    }

    public static final Optional<SeedStorageSlot> readFromNbt(NBTTagCompound tag, int invId) {
        Optional<AgriSeed> seed = AgriApi.getSeedRegistry().valueOf(new ItemStack(tag));
        if (seed.isPresent()) {
            int id = tag.getInteger(AgriNBT.ID);
            int count = tag.getInteger(AgriNBT.COUNT);
            count = count < 0 ? 0 : count;
            return Optional.of(new SeedStorageSlot(seed.get(), count, id, invId));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Compares 2 SeedStorageSlots by the given stat
     */
    public static class SlotComparator implements Comparator<SeedStorageSlot> {

        private final CropStat stat;

        public SlotComparator(String statId) {
            this.stat = Arrays.stream(CropStat.values())
                    .filter(s -> s.getId().equals(statId))
                    .findFirst()
                    .orElse(CropStat.GROWTH);
        }

        @Override
        public int compare(SeedStorageSlot o1, SeedStorageSlot o2) {
            return Integer.compare(o1.getSeed().getStat().get(this.stat), o2.getSeed().getStat().get(this.stat));
        }
    }
}
