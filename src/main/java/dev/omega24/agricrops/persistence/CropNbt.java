package dev.omega24.agricrops.persistence;

import dev.omega24.agricrops.core.field.CropState;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.genetics.StandardStat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;

/** Strict versioned decoder: corrupt/unknown specimens must not become fresh seeds. */
public final class CropNbt {
    public static final String ITEM_KEY = "agricrops_specimen";
    private CropNbt() {}

    public static NBTTagCompound write(CropState state) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("schema", 1);
        tag.setString("crop", state.crop());
        tag.setString("origin", state.origin().name());
        tag.setLong("progress", state.progress());
        NBTTagCompound genome = new NBTTagCompound();
        for (StandardStat stat : StandardStat.values()) genome.setInteger(stat.id(), state.genome().get(stat));
        tag.setTag("genome", genome);
        return tag;
    }

    public static CropState read(NBTTagCompound tag) {
        Set<String> fields = new HashSet<>(Arrays.asList("schema", "crop", "origin", "progress", "genome"));
        if (!tag.getKeySet().equals(fields) || !tag.hasKey("schema", 3) || tag.getInteger("schema") != 1
                || !tag.hasKey("crop", 8) || !tag.hasKey("origin", 8)
                || !tag.hasKey("progress", 4) || !tag.hasKey("genome", 10)) {
            throw new IllegalArgumentException("Unsupported or incomplete crop specimen");
        }
        NBTTagCompound genes = tag.getCompoundTag("genome");
        if (genes.getSize() != StandardStat.values().length) throw new IllegalArgumentException("Unknown genome fields");
        StandardGenome genome = StandardGenome.neutral();
        for (StandardStat stat : StandardStat.values()) {
            if (!genes.hasKey(stat.id(), 3)) throw new IllegalArgumentException("Missing gene " + stat.id());
            genome = genome.with(stat, genes.getInteger(stat.id()));
        }
        return new CropState(tag.getString("crop"), genome, SeedOrigin.valueOf(tag.getString("origin")), tag.getLong("progress"));
    }
}
