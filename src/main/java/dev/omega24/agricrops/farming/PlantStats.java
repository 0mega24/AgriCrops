package dev.omega24.agricrops.farming;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.api.v1.adapter.IAgriAdapter;
import dev.omega24.agricrops.api.v1.attribute.LockedAttributeInstance;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import static dev.omega24.agricrops.reference.AgriCraftConfig.STAT_FORMAT;
import dev.omega24.agricrops.utility.NBTHelper;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagList;

/**
 * A plant's stat genome (crop-mod-design.md §2): a signed byte per {@link CropStat}, each in
 * {@link CropStat#MIN}..{@link CropStat#MAX}. Replaces the old flat growth/gain/strength model.
 */
public class PlantStats implements IAgriStat, IAgriAdapter<IAgriStat> {

    public static final String NBT_STATS = "agri_stats";
    public static final String NBT_ANALYZED = "agri_analyzed";
    public static final String NBT_LOCKED_ATTRIBUTES = "agri_locked_attributes";

    private static final CropStat[] STATS = CropStat.values();
    private static final Map<String, LockedAttributeInstance> NO_ATTRIBUTES = Collections.emptyMap();

    private final byte[] values;
    private final boolean analyzed;
    private final Map<String, LockedAttributeInstance> lockedAttributes;

    public PlantStats() {
        this(new byte[STATS.length], false, NO_ATTRIBUTES);
    }

    public PlantStats(boolean analyzed) {
        this(new byte[STATS.length], analyzed, NO_ATTRIBUTES);
    }

    private PlantStats(byte[] values, boolean analyzed) {
        this(values, analyzed, NO_ATTRIBUTES);
    }

    private PlantStats(byte[] values, boolean analyzed, Map<String, LockedAttributeInstance> lockedAttributes) {
        this.values = values.length == STATS.length ? values : Arrays.copyOf(values, STATS.length);
        this.analyzed = analyzed;
        this.lockedAttributes = lockedAttributes;
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
        return new PlantStats(this.values, analyzed, this.lockedAttributes);
    }

    @Override
    public IAgriStat with(CropStat stat, int value) {
        final byte[] copy = Arrays.copyOf(this.values, this.values.length);
        copy[stat.ordinal()] = CropStat.clamp(value);
        return new PlantStats(copy, this.analyzed, this.lockedAttributes);
    }

    @Override
    public Collection<LockedAttributeInstance> getLockedAttributes() {
        return this.lockedAttributes.values();
    }

    @Override
    public Optional<LockedAttributeInstance> getLockedAttribute(String attributeId) {
        return Optional.ofNullable(this.lockedAttributes.get(attributeId));
    }

    @Override
    public IAgriStat withLockedAttributes(Collection<LockedAttributeInstance> attributes) {
        final Map<String, LockedAttributeInstance> copy = new LinkedHashMap<>();
        attributes.forEach(instance -> copy.put(instance.getAttributeId(), instance));
        return new PlantStats(this.values, this.analyzed, Collections.unmodifiableMap(copy));
    }

    @Override
    public boolean writeToNBT(NBTTagCompound tag) {
        tag.setTag(NBT_STATS, new NBTTagByteArray(this.values));
        tag.setBoolean(NBT_ANALYZED, analyzed);
        if (!this.lockedAttributes.isEmpty()) {
            final NBTTagList list = new NBTTagList();
            this.lockedAttributes.values().forEach(instance -> {
                final NBTTagCompound instanceTag = new NBTTagCompound();
                instance.writeToNBT(instanceTag);
                list.appendTag(instanceTag);
            });
            tag.setTag(NBT_LOCKED_ATTRIBUTES, list);
        }
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
            for (LockedAttributeInstance instance : this.lockedAttributes.values()) {
                lines.accept(MessageFormat.format(STAT_FORMAT,
                        AgriCore.getTranslator().translate("agricrops_tooltip." + instance.getAttributeId()),
                        instance.getExpressedValue(), CropStat.MAX));
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
            final Map<String, LockedAttributeInstance> attributes;
            if (tag.hasKey(NBT_LOCKED_ATTRIBUTES)) {
                final NBTTagList list = tag.getTagList(NBT_LOCKED_ATTRIBUTES, 10);
                final Map<String, LockedAttributeInstance> parsed = new LinkedHashMap<>();
                for (int i = 0; i < list.tagCount(); i++) {
                    final LockedAttributeInstance instance = LockedAttributeInstance.readFromNBT(list.getCompoundTagAt(i));
                    parsed.put(instance.getAttributeId(), instance);
                }
                attributes = Collections.unmodifiableMap(parsed);
            } else {
                attributes = NO_ATTRIBUTES;
            }
            return Optional.of(new PlantStats(
                    tag.getByteArray(NBT_STATS),
                    tag.getBoolean(NBT_ANALYZED),
                    attributes
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
        hash = 41 * hash + this.lockedAttributes.hashCode();
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
        return Arrays.equals(this.values, other.values) && this.analyzed == other.analyzed
                && this.lockedAttributes.equals(other.lockedAttributes);
    }

}
