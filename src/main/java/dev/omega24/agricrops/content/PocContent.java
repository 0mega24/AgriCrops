package dev.omega24.agricrops.content;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.environment.Factor;
import dev.omega24.agricrops.core.field.SoilProfile;
import dev.omega24.agricrops.core.genetics.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.ResourceLocation;

/** One pending author model. Defaults and real CT submissions share validation/freeze. */
public final class PocContent {
    private static boolean frozen;
    private static final Map<String, Factor> FACTORS = new TreeMap<>();
    private static final Map<String, SoilProfile> SOILS = new TreeMap<>();
    private static final Map<String, TraitDefinition> TRAITS = new TreeMap<>();
    private static final List<Breeding.Unlock> UNLOCKS = new ArrayList<>();
    private static final Map<String, Map<String, Double>> BIOMES = new TreeMap<>();
    private static final Map<Integer, Map<String, Double>> DIMENSIONS = new TreeMap<>();
    static {
        factor(new Factor("agricrops:light", 0, 15, "light level", null), false);
        factor(new Factor("agricrops:temperature", -10, 10, "Minecraft temperature", null), false);
        for (String id : Arrays.asList("humidity", "air_quality", "moisture", "nutrition", "aeration", "rain", "sky")) factor(new Factor("agricrops:" + id, 0, 1, "game index (not physical percent)", null), false);
        factor(new Factor("agricrops:height", 0, 255, "block Y", null), false);
        factor(new Factor("agricrops:dimension", -1000000, 1000000, "dimension ID", null), false);
        soil(new SoilProfile("minecraft:farmland", 10000, 10000, 1, 10, 10, 1000, 0.9), false);
        soil(new SoilProfile("minecraft:dirt", 7000, 7000, 1, 20, 8, 600, 0.85), false);
        soil(new SoilProfile("minecraft:grass", 7000, 7000, 1, 20, 8, 600, 0.85), false);
        soil(new SoilProfile("minecraft:clay", 10000, 6000, 0, 2, 5, 200, 0.7), false);
        soil(new SoilProfile("minecraft:sand", 3000, 2000, 6, 80, 4, 400, 0.8), false);
        soil(new SoilProfile("minecraft:gravel", 2500, 2000, 4, 100, 4, 600, 0.8), false);
        trait(new TraitDefinition("agricrops:frost_hardiness", "agricrops:climate", TraitDefinition.Effect.COLD_TOLERANCE, "agricrops:temperature", 0.6, 2, 3, "agricrops.trait.frost_hardiness"), false);
        trait(new TraitDefinition("agricrops:heat_hardiness", "agricrops:climate", TraitDefinition.Effect.HEAT_TOLERANCE, "agricrops:temperature", 0.6, 2, 3, "agricrops.trait.heat_hardiness"), false);
        trait(new TraitDefinition("agricrops:deep_roots", "agricrops:roots", TraitDefinition.Effect.ROOT_DEPTH, "agricrops:moisture", 1, 2, 3, "agricrops.trait.deep_roots"), false);
        unlock(new Breeding.Unlock("agricrops:wheat", "agricrops:beetroot", "agricrops:frost_hardiness"));
        unlock(new Breeding.Unlock("agricrops:wheat", "agricrops:carrot", "agricrops:heat_hardiness"));
        unlock(new Breeding.Unlock("agricrops:beetroot", "agricrops:carrot", "agricrops:deep_roots"));
        biome("minecraft:desert", "agricrops:air_quality", 0.85);
        biome("minecraft:hell", "agricrops:air_quality", 0.35);
    }
    private PocContent() {}
    private static void writable() { if (frozen) throw new IllegalStateException("Definitions frozen; restart after author edits"); }
    private static <T> void put(Map<String, T> map, String key, T value, boolean replace) {
        writable(); if (map.containsKey(key) != replace) throw new IllegalArgumentException(replace ? "Missing override target " + key : "Duplicate definition " + key); map.put(key, value);
    }
    public static void factor(Factor value, boolean replace) { put(FACTORS, value.id, value, replace); }
    public static void soil(SoilProfile value, boolean replace) { put(SOILS, value.block, value, replace); }
    public static void trait(TraitDefinition value, boolean replace) { put(TRAITS, value.id, value, replace); }
    public static void unlock(Breeding.Unlock rule) { writable(); UNLOCKS.add(rule); }
    public static void biome(String biome, String factor, double value) { writable(); CropDefinition.identifier(biome); CropDefinition.identifier(factor); if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite override"); BIOMES.computeIfAbsent(biome, ignored -> new TreeMap<>()).put(factor, value); }
    public static void dimension(int dimension, String factor, double value) { writable(); CropDefinition.identifier(factor); if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite override"); DIMENSIONS.computeIfAbsent(dimension, ignored -> new TreeMap<>()).put(factor, value); }
    public static void freeze(Map<String, CropDefinition> crops) {
        writable();
        if (FACTORS.size() > 32) throw new IllegalArgumentException("At most 32 environmental factors are supported");
        for (Factor factor : FACTORS.values()) if (!dev.omega24.agricrops.world.WorldEnvironment.available(factor)) throw new IllegalArgumentException("Factor has no observation provider: " + factor.id);
        for (SoilProfile soil : SOILS.values()) if (!Block.REGISTRY.containsKey(new ResourceLocation(soil.block))) throw new IllegalArgumentException("Missing soil " + soil.block);
        for (TraitDefinition trait : TRAITS.values()) if (!FACTORS.containsKey(trait.factor)) throw new IllegalArgumentException("Unknown trait factor " + trait.factor);
        for (CropDefinition crop : crops.values()) {
            if (!SOILS.containsKey(crop.soil())) throw new IllegalArgumentException(crop.id() + ": soil needs a profile");
            crop.rules().requirements.forEach(rule -> { Factor factor = FACTORS.get(rule.factor); if (factor == null) throw new IllegalArgumentException("Unavailable factor provider " + rule.factor); factor.validate(rule.minimum); factor.validate(rule.maximum); });
            crop.rules().foundations.values().forEach(block -> { if (!Block.REGISTRY.containsKey(new ResourceLocation(block)) || !SOILS.containsKey(block)) throw new IllegalArgumentException("Foundation needs an existing soil profile: " + block); });
        }
        for (Breeding.Unlock rule : UNLOCKS) if (!crops.containsKey(rule.donor) || !crops.containsKey(rule.recipient) || !TRAITS.containsKey(rule.trait)) throw new IllegalArgumentException("Invalid trait unlock reference");
        BIOMES.forEach((biome, values) -> { if (!Biome.REGISTRY.containsKey(new ResourceLocation(biome))) throw new IllegalArgumentException("Missing biome " + biome); validateOverrides(values); });
        DIMENSIONS.values().forEach(PocContent::validateOverrides);
        frozen = true;
    }
    private static void validateOverrides(Map<String, Double> values) {
        values.forEach((id, value) -> { Factor factor = FACTORS.get(id); if (factor == null) throw new IllegalArgumentException("Missing override factor " + id); factor.validate(value); });
    }
    public static Map<String, Factor> factors() { return Collections.unmodifiableMap(FACTORS); }
    public static Map<String, SoilProfile> soils() { return Collections.unmodifiableMap(SOILS); }
    public static Map<String, TraitDefinition> traits() { return Collections.unmodifiableMap(TRAITS); }
    public static List<Breeding.Unlock> unlocks() { return Collections.unmodifiableList(UNLOCKS); }
    public static Map<String, Double> biome(String id) { return Collections.unmodifiableMap(BIOMES.getOrDefault(id, Collections.emptyMap())); }
    public static Map<String, Double> dimension(int id) { return Collections.unmodifiableMap(DIMENSIONS.getOrDefault(id, Collections.emptyMap())); }
    public static void validate(Specimen specimen, Map<String, CropDefinition> crops) {
        CropDefinition body = crops.get(specimen.body);
        if (body == null) throw new IllegalArgumentException("Removed body definition");
        Map<Integer, String> foundations = new TreeMap<>(body.rules().foundations);
        for (String id : specimen.products.weights().keySet()) {
            CropDefinition component = crops.get(id);
            if (component == null || !component.rules().family.equals(body.rules().family)) throw new IllegalArgumentException("Removed/incompatible output component");
            if (!id.equals(body.id()) && !component.rules().breedable) throw new IllegalArgumentException("Source no longer permits hybrid output");
            if (!component.soil().equals(body.soil())) throw new IllegalArgumentException("Incompatible component surface soil");
            component.rules().foundations.forEach((depth, block) -> { String previous = foundations.put(depth, block); if (previous != null && !previous.equals(block)) throw new IllegalArgumentException("Conflicting saved foundations"); });
        }
        if (!body.rules().loci.containsAll(specimen.traits.loci().keySet())) throw new IllegalArgumentException("Ineligible saved locus");
        specimen.traits.expressed(TRAITS);
    }
}
