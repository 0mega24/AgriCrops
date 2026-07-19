package dev.omega24.agricrops.farming;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.adapter.IAgriAdapter;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import static dev.omega24.agricrops.reference.AgriCraftConfig.STAT_FORMAT;
import dev.omega24.agricrops.utility.NBTHelper;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagByteArray;

/**
 * A plant's stat genome (crop-mod-design.md §2): a signed byte per {@link CropStat}, each in
 * {@link CropStat#MIN}..{@link CropStat#MAX}. Replaces the old flat growth/gain/strength model.
 */
public class PlantStats implements IAgriStat, IAgriAdapter<IAgriStat> {

    public static final String NBT_STATS = "agri_stats";
    public static final String NBT_ANALYZED = "agri_analyzed";

    private static final CropStat[] STATS = CropStat.values();

    private final byte[] values;
    private final boolean analyzed;

    public PlantStats() {
        this(new byte[STATS.length], false);
    }

    public PlantStats(boolean analyzed) {
        this(new byte[STATS.length], analyzed);
    }

    private PlantStats(byte[] values, boolean analyzed) {
        this.values = values.length == STATS.length ? values : Arrays.copyOf(values, STATS.length);
        this.analyzed = analyzed;
    }

    @Override
    public boolean isAnalyzed() {
        return this.analyzed;
    }

    @Override
    public byte get(CropStat stat) {
        return this.values[stat.ordinal()];
    }

    @Override
    public IAgriStat withAnalyzed(boolean analyzed) {
        return new PlantStats(this.values, analyzed);
    }

    @Override
    public IAgriStat with(CropStat stat, int value) {
        final byte[] copy = Arrays.copyOf(this.values, this.values.length);
        copy[stat.ordinal()] = CropStat.clamp(value);
        return new PlantStats(copy, this.analyzed);
    }

    @Override
    public boolean writeToNBT(NBTTagCompound tag) {
        tag.setTag(NBT_STATS, new NBTTagByteArray(this.values));
        tag.setBoolean(NBT_ANALYZED, analyzed);
        return true;
    }

    @Override
    public boolean addStats(Consumer<String> lines) {
        try {
            for (CropStat stat : STATS) {
                lines.accept(MessageFormat.format(STAT_FORMAT,
                        AgriCore.getTranslator().translate(stat.getTranslationKey()),
                        this.get(stat), CropStat.MAX));
            }
            return true;
        } catch (IllegalArgumentException e) {
            lines.accept("Invalid Stat Format!");
            return false;
        }
    }

    @Override
    public boolean accepts(Object obj) {
        NBTTagCompound tag = NBTHelper.asTag(obj);
        return tag != null && NBTHelper.hasKey(tag, NBT_STATS, NBT_ANALYZED);
    }

    @Override
    public Optional<IAgriStat> valueOf(Object obj) {
        NBTTagCompound tag = NBTHelper.asTag(obj);
        if (tag != null) {
            return Optional.of(new PlantStats(
                    tag.getByteArray(NBT_STATS),
                    tag.getBoolean(NBT_ANALYZED)
            ));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Arrays.hashCode(this.values);
        hash = 41 * hash + (this.analyzed ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PlantStats)) {
            return false;
        }
        final PlantStats other = (PlantStats) obj;
        return Arrays.equals(this.values, other.values) && this.analyzed == other.analyzed;
    }

}
