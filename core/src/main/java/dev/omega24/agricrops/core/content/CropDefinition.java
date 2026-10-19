package dev.omega24.agricrops.core.content;

import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.growth.GrowthTimeline;
import java.util.Objects;

/** Resolved symbolic content; no Minecraft objects or translated client strings. */
public final class CropDefinition {
    private final String id, seed, product, soil, nameKey;
    private final GrowthTimeline timeline;
    private final StandardGenome baseline;
    private final int waterCost, nutrientCost, minLight, maxLight;

    public CropDefinition(String id, String seed, String product, String soil, String nameKey,
                          GrowthTimeline timeline, StandardGenome baseline,
                          int waterCost, int nutrientCost, int minLight, int maxLight) {
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
    public int waterCost() { return waterCost; }
    public int nutrientCost() { return nutrientCost; }
    public int minLight() { return minLight; }
    public int maxLight() { return maxLight; }
}
