package dev.omega24.agricrops.core.content;

import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.growth.GrowthTimeline;
import java.util.Objects;

/** Resolved symbolic content; no Minecraft objects or translated client strings. */
public final class CropDefinition {
    /** Current crop-sticks age IDs. Biological phase windows remain independent. */
    public static final int MATURE_STAGE = 7;
    private final String id, seed, product, soil, nameKey;
    private final GrowthTimeline timeline;
    private final StandardGenome baseline;
    private final int regrowthStage;
    private final long regrowthTicks;
    private final int waterCost, nutrientCost, minLight, maxLight;

    public CropDefinition(String id, String seed, String product, String soil, String nameKey,
                          GrowthTimeline timeline, StandardGenome baseline,
                          int waterCost, int nutrientCost, int minLight, int maxLight) {
        this(id, seed, product, soil, nameKey, timeline, baseline, waterCost, nutrientCost, minLight, maxLight,
                1);
    }

    public CropDefinition(String id, String seed, String product, String soil, String nameKey,
                          GrowthTimeline timeline, StandardGenome baseline,
                          int waterCost, int nutrientCost, int minLight, int maxLight, int regrowthStage) {
        this.id = identifier(id);
        this.seed = identifier(seed);
        this.product = identifier(product);
        this.soil = identifier(soil);
        if (nameKey == null || !nameKey.matches("[a-z0-9_.:-]+")) {
            throw new IllegalArgumentException("A translation key is required for " + id);
        }
        this.nameKey = nameKey;
        this.timeline = Objects.requireNonNull(timeline, "timeline");
        this.baseline = Objects.requireNonNull(baseline, "baseline");
        if (timeline.totalTicks() > 288000L) throw new IllegalArgumentException("Crop duration exceeds 288000 active ticks");
        if (regrowthStage < 0 || regrowthStage >= MATURE_STAGE) {
            throw new IllegalArgumentException("regrowFrom must be a numeric stage in 0..6; stage 7 is mature");
        }
        this.regrowthStage = regrowthStage;
        this.regrowthTicks = (timeline.totalTicks() * regrowthStage + MATURE_STAGE - 1) / MATURE_STAGE;
        if (stageAt(regrowthTicks) != regrowthStage) {
            throw new IllegalArgumentException("Growth duration is too short to represent regrowth stage " + regrowthStage);
        }
        if (waterCost < 1 || waterCost > 100 || nutrientCost < 1 || nutrientCost > 100) {
            throw new IllegalArgumentException("Resource costs must be 1..100 units per development tick");
        }
        if (minLight < 0 || maxLight > 15 || minLight > maxLight) {
            throw new IllegalArgumentException("Light range must be ordered within 0..15");
        }
        this.waterCost = waterCost;
        this.nutrientCost = nutrientCost;
        this.minLight = minLight;
        this.maxLight = maxLight;
    }

    public static String identifier(String id) {
        if (id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Expected namespaced identifier, got " + id);
        }
        return id;
    }

    public String id() { return id; }
    public String seed() { return seed; }
    public String product() { return product; }
    public String soil() { return soil; }
    public String nameKey() { return nameKey; }
    public GrowthTimeline timeline() { return timeline; }
    public StandardGenome baseline() { return baseline; }
    public int regrowthStage() { return regrowthStage; }
    public long regrowthTicks() { return regrowthTicks; }
    public int stageAt(long progress) {
        if (progress < 0) throw new IllegalArgumentException("Growth progress cannot be negative");
        return progress >= timeline.totalTicks() ? MATURE_STAGE
                : (int) (progress * MATURE_STAGE / timeline.totalTicks());
    }
    public int waterCost() { return waterCost; }
    public int nutrientCost() { return nutrientCost; }
    public int minLight() { return minLight; }
    public int maxLight() { return maxLight; }
}
