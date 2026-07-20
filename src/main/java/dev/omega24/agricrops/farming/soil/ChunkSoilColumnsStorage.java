/*
 */
package dev.omega24.agricrops.farming.soil;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

/**
 * NBT round-trip for {@link IChunkSoilColumns}, so nutrient pools survive a chunk
 * unload/reload.
 */
public class ChunkSoilColumnsStorage implements Capability.IStorage<IChunkSoilColumns> {

    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_Z = "z";
    private static final String KEY_COMPOSITION = "composition";
    private static final String KEY_DEPTH = "depth";
    private static final String KEY_NUTRIENT_CAPACITY = "nutrient_capacity";
    private static final String KEY_NUTRIENTS = "nutrients";
    private static final String KEY_MOISTURE = "moisture";

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IChunkSoilColumns> capability, IChunkSoilColumns instance, EnumFacing side) {
        final NBTTagList list = new NBTTagList();
        if (instance instanceof ChunkSoilColumns) {
            for (Map.Entry<BlockPos, ISoilColumn> entry : ((ChunkSoilColumns) instance).getColumns().entrySet()) {
                final BlockPos pos = entry.getKey();
                final ISoilColumn column = entry.getValue();
                final NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger(KEY_X, pos.getX());
                tag.setInteger(KEY_Y, pos.getY());
                tag.setInteger(KEY_Z, pos.getZ());
                tag.setString(KEY_COMPOSITION, column.getComposition().getId());
                tag.setInteger(KEY_DEPTH, column.getDepth());
                tag.setInteger(KEY_NUTRIENT_CAPACITY, column.getNutrientCapacity());
                tag.setInteger(KEY_NUTRIENTS, column.getNutrients());
                tag.setInteger(KEY_MOISTURE, column.getMoisture());
                list.appendTag(tag);
            }
        }
        return list;
    }

    @Override
    public void readNBT(Capability<IChunkSoilColumns> capability, IChunkSoilColumns instance, EnumFacing side, NBTBase nbt) {
        if (!(instance instanceof ChunkSoilColumns) || !(nbt instanceof NBTTagList)) {
            return;
        }
        final Map<BlockPos, ISoilColumn> columns = ((ChunkSoilColumns) instance).getColumns();
        final NBTTagList list = (NBTTagList) nbt;
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound tag = list.getCompoundTagAt(i);
            final ISoilComposition composition = AgriApi.getSoilCompositionRegistry()
                    .get(tag.getString(KEY_COMPOSITION))
                    .orElse(null);
            if (composition == null) {
                continue;
            }
            final BlockPos pos = new BlockPos(tag.getInteger(KEY_X), tag.getInteger(KEY_Y), tag.getInteger(KEY_Z));
            columns.put(pos, new SoilColumn(tag.getInteger(KEY_DEPTH), composition,
                    tag.getInteger(KEY_NUTRIENT_CAPACITY), tag.getInteger(KEY_NUTRIENTS),
                    tag.getInteger(KEY_MOISTURE)));
        }
    }

}
