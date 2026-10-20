package dev.omega24.agricrops.persistence;

import dev.omega24.agricrops.core.field.SoilCell;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

/** Dimension-local position ownership, ready to address more root-zone cells without duplicate pools. */
public final class FieldSoils extends WorldSavedData {
    private static final String NAME = "agricrops_soils_v1";
    private final Map<Long, SoilCell> cells = new HashMap<>();
    private final Map<Long, Long> processed = new HashMap<>();
    private NBTTagCompound rejected;
    public FieldSoils() { super(NAME); }
    public FieldSoils(String name) { super(name); }

    public static FieldSoils get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        FieldSoils soils = (FieldSoils) storage.getOrLoadData(FieldSoils.class, NAME);
        if (soils == null) { soils = new FieldSoils(); storage.setData(NAME, soils); soils.markDirty(); }
        soils.requireReadable();
        return soils;
    }

    public SoilCell observe(BlockPos pos, String actualBlock) {
        requireReadable();
        SoilCell old = cells.get(pos.toLong());
        // Unobserved blocks do not mint fertilizer. All initial reserves come from explicit inputs.
        SoilCell next = old == null ? new SoilCell(actualBlock, 0, 0) : old.reconcile(actualBlock);
        if (old != next) put(pos, next);
        return next;
    }

    public void put(BlockPos pos, SoilCell cell) { requireReadable(); cells.put(pos.toLong(), cell); markDirty(); }
    public boolean beginInterval(BlockPos pos, long interval) {
        Long previous = processed.put(pos.toLong(), interval); return previous == null || previous.longValue() != interval;
    }

    public void reconcileTracked(BlockPos pos, String block) {
        requireReadable();
        SoilCell old = cells.get(pos.toLong());
        if (old != null && !old.block().equals(block)) put(pos, old.reconcile(block));
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        if (rejected != null) { tag.merge(rejected); return tag; }
        tag.setInteger("schema", 2);
        NBTTagList list = new NBTTagList();
        cells.forEach((position, cell) -> {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setLong("position", position);
            entry.setString("block", cell.block());
            entry.setInteger("water", cell.water()); entry.setInteger("nutrients", cell.nutrients());
            entry.setInteger("organic", cell.organic());
            list.appendTag(entry);
        });
        tag.setTag("cells", list); return tag;
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        try { decode(tag); rejected = null; }
        catch (RuntimeException exception) { rejected = tag.copy(); throw exception; }
    }

    private void requireReadable() {
        if (rejected != null) throw new IllegalStateException("Agricrops soil data was rejected; restore a compatible version/backup before farming");
    }

    private void decode(NBTTagCompound tag) {
        if (!tag.getKeySet().equals(new HashSet<>(Arrays.asList("schema", "cells")))
                || !tag.hasKey("schema", 3) || (tag.getInteger("schema") != 1 && tag.getInteger("schema") != 2) || !tag.hasKey("cells", 9)) {
            throw new IllegalArgumentException("Unsupported Agricrops soil data; restore a compatible version/backup");
        }
        Map<Long, SoilCell> decoded = new HashMap<>();
        NBTTagList list = tag.getTagList("cells", 10);
        NBTTagList original = (NBTTagList) tag.getTag("cells");
        if (original.tagCount() > 0 && original.getTagType() != 10) throw new IllegalArgumentException("Invalid soil cell list type");
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            Set<String> fields = new HashSet<>(Arrays.asList("position", "block", "water", "nutrients"));
            if (tag.getInteger("schema") == 2) fields.add("organic");
            if (!entry.getKeySet().equals(fields)
                    || !entry.hasKey("position", 4) || !entry.hasKey("block", 8)
                    || !entry.hasKey("water", 3) || !entry.hasKey("nutrients", 3)
                    || (tag.getInteger("schema") == 2 && !entry.hasKey("organic", 3))) {
                throw new IllegalArgumentException("Incomplete Agricrops soil cell");
            }
            if (decoded.put(entry.getLong("position"), new SoilCell(entry.getString("block"),
                    entry.getInteger("water"), entry.getInteger("nutrients"), entry.getInteger("organic"))) != null) {
                throw new IllegalArgumentException("Duplicate soil position");
            }
        }
        cells.clear(); cells.putAll(decoded);
    }
}
