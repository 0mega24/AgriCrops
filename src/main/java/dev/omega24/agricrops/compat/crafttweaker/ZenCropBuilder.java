package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.content.PlantRules;
import dev.omega24.agricrops.core.environment.Requirement;
import dev.omega24.agricrops.core.growth.GrowthPhase;
import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.genetics.StandardStat;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.agricrops.CropBuilder")
public final class ZenCropBuilder {
    private final String id, seed, product, nameKey;
    private String soil = "minecraft:farmland";
    private int regrowthStage = 1;
    private int germination = 1200, vegetative = 1200, flowering = 1200, fruiting = 1200;
    private int water = 1, nutrients = 1, minLight = 9, maxLight = 15;
    private int[] stats = new int[8];
    private final PlantRules.Builder rules = PlantRules.builder().roots(3).harvestUnits(2)
            .requirement(new Requirement("agricrops:temperature", 0.3, 1.2, 1, false))
            .requirement(new Requirement("agricrops:humidity", 0.25, 0.8, 0.8, false))
            .requirement(new Requirement("agricrops:air_quality", 0.65, 1, 0.65, false))
            .requirement(new Requirement("agricrops:aeration", 0.3, 1, 0.4, false));

    ZenCropBuilder(String id, String seed, String product, String nameKey) {
        this.id = id; this.seed = seed; this.product = product; this.nameKey = nameKey;
    }

    @ZenMethod public ZenCropBuilder soil(String id) { this.soil = id; return this; }
    @ZenMethod public ZenCropBuilder regrowFrom(int stage) { regrowthStage = stage; return this; }
    @ZenMethod public ZenCropBuilder phaseTicks(int germination, int vegetative, int flowering, int fruiting) {
        this.germination = germination; this.vegetative = vegetative;
        this.flowering = flowering; this.fruiting = fruiting; return this;
    }
    @ZenMethod public ZenCropBuilder costs(int waterPerGrowthTick, int nutrientsPerGrowthTick) {
        water = waterPerGrowthTick; nutrients = nutrientsPerGrowthTick; return this;
    }
    @ZenMethod public ZenCropBuilder light(int minimum, int maximum) {
        minLight = minimum; maxLight = maximum; return this;
    }
    @ZenMethod public ZenCropBuilder baseStats(int[] stats) { this.stats = stats.clone(); return this; }
    @ZenMethod public ZenCropBuilder visual(String profile) { rules.visual(profile); return this; }
    @ZenMethod public ZenCropBuilder itemMetadata(int seed, int product) { rules.itemMetadata(seed, product); return this; }
    @ZenMethod public ZenCropBuilder itemNbt(String seed, String product) { rules.itemNbt(seed, product); return this; }
    @ZenMethod public ZenCropBuilder compatibility(String family) { rules.family(family); return this; }
    @ZenMethod public ZenCropBuilder roots(int depth) { rules.roots(depth); return this; }
    @ZenMethod public ZenCropBuilder harvestUnits(int count) { rules.harvestUnits(count); return this; }
    @ZenMethod public ZenCropBuilder requirement(String factor, double min, double max, double tolerance, boolean hard) {
        return configure(() -> rules.requirement(new Requirement(factor, min, max, tolerance, hard)));
    }
    @ZenMethod public ZenCropBuilder foundation(int depth, String block) { return configure(() -> rules.foundation(depth, block)); }
    @ZenMethod public ZenCropBuilder windows(String donor, String recipient) {
        return configure(() -> rules.windows(GrowthPhase.valueOf(donor.toUpperCase(java.util.Locale.ROOT)), GrowthPhase.valueOf(recipient.toUpperCase(java.util.Locale.ROOT))));
    }
    @ZenMethod public ZenCropBuilder propagation(boolean clipping, boolean breeding, int rootingTicks) {
        rules.clippable(clipping).breedable(breeding).rootingTicks(rootingTicks); return this;
    }
    @ZenMethod public ZenCropBuilder reproductionCosts(int water, int nutrients) { rules.reproductionCost(water, nutrients); return this; }
    @ZenMethod public ZenCropBuilder loci(String[] ids) { return configure(() -> rules.loci(ids)); }
    @ZenMethod public void register() { submit(false); }
    @ZenMethod public void replace() { submit(true); }
    private ZenCropBuilder configure(Runnable action) {
        try { action.run(); } catch (RuntimeException exception) {
            String error = CraftTweakerAPI.getScriptFileAndLine() + " / " + id + ": " + exception.getMessage();
            CropDefinitions.error(error); CraftTweakerAPI.logError(error);
        }
        return this;
    }

    private void submit(boolean replace) {
        String source = CraftTweakerAPI.getScriptFileAndLine();
        try {
            if (stats.length != 8) throw new IllegalArgumentException("baseStats needs exactly eight values");
            StandardGenome genome = StandardGenome.neutral();
            for (StandardStat stat : StandardStat.values()) genome = genome.with(stat, stats[stat.ordinal()]);
            CropDefinitions.submit(new CropDefinition(id, seed, product, soil, nameKey,
                    CropDefinitions.timeline(germination, vegetative, flowering, fruiting), genome,
                    water, nutrients, minLight, maxLight, regrowthStage).withRules(rules.build()), replace);
            CraftTweakerAPI.logInfo("Agricrops: queued " + id + " from " + source + " (restart required)");
        } catch (RuntimeException exception) {
            String error = source + " / " + id + ": " + exception.getMessage();
            CropDefinitions.error(error);
            CraftTweakerAPI.logError(error);
        }
    }
}
