package dev.omega24.agricrops.persistence;

import dev.omega24.agricrops.core.field.*;
import dev.omega24.agricrops.core.genetics.*;
import java.util.*;
import net.minecraft.nbt.NBTTagCompound;

/** Strict versioned decoder. Seed items carry an unfunded specimen, never stored production. */
public final class CropNbt {
    public static final String ITEM_KEY = "agricrops_specimen";
    private CropNbt() {}
    public static NBTTagCompound seed(Specimen specimen) { return write(CropState.plant(specimen, 0)); }

    public static NBTTagCompound write(CropState state) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("schema", 2);
        tag.setString("crop", state.crop());
        tag.setString("origin", state.origin().name());
        tag.setLong("progress", state.progress());
        tag.setLong("cycleStart", state.cycleStart()); tag.setLong("rooting", state.rooting());
        tag.setLong("lineage", state.specimen().lineage); tag.setInteger("generation", state.specimen().generation);
        NBTTagCompound genome = new NBTTagCompound();
        for (StandardStat stat : StandardStat.values()) genome.setInteger(stat.id(), state.genome().get(stat));
        tag.setTag("genome", genome);
        tag.setTag("products", integers(state.specimen().products.weights())); tag.setTag("credit", integers(state.credit()));
        NBTTagCompound loci = new NBTTagCompound();
        state.specimen().traits.loci().forEach((id, pair) -> {
            NBTTagCompound p = new NBTTagCompound(); p.setString("first", pair.first); p.setString("second", pair.second);
            p.setInteger("generations", pair.generations); p.setBoolean("fixed", pair.fixed); loci.setTag(id, p);
        });
        tag.setTag("loci", loci); tag.setInteger("paidWater", state.paidWater()); tag.setInteger("paidNutrients", state.paidNutrients());
        if (state.plan() != null) {
            NBTTagCompound plan = new NBTTagCompound(); plan.setTag("products", integers(state.plan().products));
            plan.setTag("credit", integers(state.plan().nextCredit)); plan.setInteger("water", state.plan().water);
            plan.setInteger("nutrients", state.plan().nutrients); tag.setTag("plan", plan);
        }
        return tag;
    }

    public static CropState read(NBTTagCompound tag) {
        require(tag, "schema", 3); int version = tag.getInteger("schema");
        if (version != 1 && version != 2) throw new IllegalArgumentException("Unsupported specimen schema " + version);
        Set<String> fields = new HashSet<>(Arrays.asList("schema", "crop", "origin", "progress", "genome"));
        if (version == 2) {
            fields.addAll(Arrays.asList("cycleStart", "rooting", "lineage", "generation", "products", "credit", "loci", "paidWater", "paidNutrients"));
            if (tag.hasKey("plan")) fields.add("plan");
        }
        if (!tag.getKeySet().equals(fields)) throw new IllegalArgumentException("Unknown/incomplete specimen fields");
        require(tag, "crop", 8); require(tag, "origin", 8); require(tag, "progress", 4); require(tag, "genome", 10);
        NBTTagCompound genes = tag.getCompoundTag("genome");
        if (genes.getSize() != StandardStat.values().length) throw new IllegalArgumentException("Unknown genome fields");
        StandardGenome genome = StandardGenome.neutral();
        for (StandardStat stat : StandardStat.values()) {
            if (!genes.hasKey(stat.id(), 3)) throw new IllegalArgumentException("Missing gene " + stat.id());
            genome = genome.with(stat, genes.getInteger(stat.id()));
        }
        SeedOrigin origin = SeedOrigin.valueOf(tag.getString("origin"));
        // Legacy growth was not backed by a persisted production budget. Do not invent one.
        if (version == 1) return new CropState(tag.getString("crop"), genome, origin, tag.getLong("progress"));
        for (String key : Arrays.asList("cycleStart", "rooting", "lineage")) require(tag, key, 4);
        for (String key : Arrays.asList("generation", "paidWater", "paidNutrients")) require(tag, key, 3);
        require(tag, "loci", 10); NBTTagCompound loci = tag.getCompoundTag("loci"); Map<String, TraitGenome.Pair> pairs = new TreeMap<>();
        if (loci.getSize() > 8) throw new IllegalArgumentException("Too many loci");
        for (String id : loci.getKeySet()) {
            require(loci, id, 10); NBTTagCompound p = loci.getCompoundTag(id); exact(p, "first", "second", "generations", "fixed");
            require(p, "first", 8); require(p, "second", 8); require(p, "generations", 3); require(p, "fixed", 1);
            if (p.getByte("fixed") != 0 && p.getByte("fixed") != 1) throw new IllegalArgumentException("Invalid fixed flag");
            pairs.put(id, new TraitGenome.Pair(p.getString("first"), p.getString("second"), p.getInteger("generations"), p.getBoolean("fixed")));
        }
        Specimen specimen = new Specimen(tag.getString("crop"), genome, origin, new ProductMix(integers(tag, "products")),
                new TraitGenome(pairs), tag.getLong("lineage"), tag.getInteger("generation"));
        ProductionPlan plan = null;
        if (tag.hasKey("plan")) {
            require(tag, "plan", 10); NBTTagCompound p = tag.getCompoundTag("plan"); exact(p, "products", "credit", "water", "nutrients");
            require(p, "water", 3); require(p, "nutrients", 3);
            plan = new ProductionPlan(integers(p, "products"), integers(p, "credit"), p.getInteger("water"), p.getInteger("nutrients"));
        }
        return new CropState(specimen, tag.getLong("progress"), tag.getLong("cycleStart"), tag.getLong("rooting"), plan,
                tag.getInteger("paidWater"), tag.getInteger("paidNutrients"), integers(tag, "credit"));
    }
    private static NBTTagCompound integers(Map<String, Integer> values) {
        NBTTagCompound tag = new NBTTagCompound(); values.forEach(tag::setInteger); return tag;
    }
    private static Map<String, Integer> integers(NBTTagCompound parent, String key) {
        require(parent, key, 10); NBTTagCompound tag = parent.getCompoundTag(key);
        if (tag.getSize() > 4) throw new IllegalArgumentException("Oversized component map");
        Map<String, Integer> values = new TreeMap<>();
        for (String id : tag.getKeySet()) { require(tag, id, 3); values.put(id, tag.getInteger(id)); } return values;
    }
    private static void require(NBTTagCompound tag, String key, int type) {
        if (!tag.hasKey(key, type)) throw new IllegalArgumentException("Missing/wrong type: " + key);
    }
    private static void exact(NBTTagCompound tag, String... keys) {
        if (!tag.getKeySet().equals(new HashSet<>(Arrays.asList(keys)))) throw new IllegalArgumentException("Unknown/incomplete fields");
    }
}
