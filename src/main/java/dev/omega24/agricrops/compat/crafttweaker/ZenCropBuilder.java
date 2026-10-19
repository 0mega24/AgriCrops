package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.core.content.CropDefinition;
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
    @ZenMethod public void register() { submit(false); }
    @ZenMethod public void replace() { submit(true); }

    private void submit(boolean replace) {
        String source = CraftTweakerAPI.getScriptFileAndLine();
        try {
            if (stats.length != 8) throw new IllegalArgumentException("baseStats needs exactly eight values");
            StandardGenome genome = StandardGenome.neutral();
            for (StandardStat stat : StandardStat.values()) genome = genome.with(stat, stats[stat.ordinal()]);
            CropDefinitions.submit(new CropDefinition(id, seed, product, soil, nameKey,
                    CropDefinitions.timeline(germination, vegetative, flowering, fruiting), genome,
                    water, nutrients, minLight, maxLight, regrowthStage), replace);
            CraftTweakerAPI.logInfo("Agricrops: queued " + id + " from " + source + " (restart required)");
        } catch (RuntimeException exception) {
            String error = source + " / " + id + ": " + exception.getMessage();
            CropDefinitions.error(error);
            CraftTweakerAPI.logError(error);
        }
    }
}
