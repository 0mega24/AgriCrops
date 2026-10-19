package dev.omega24.agricrops.content;

import dev.omega24.agricrops.core.content.CropCatalog;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.genetics.StandardGenome;
import dev.omega24.agricrops.core.growth.GrowthPhase;
import dev.omega24.agricrops.core.growth.GrowthTimeline;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/** CT calls may arrive before our preInit. Java defaults therefore belong to this pure pending store. */
public final class CropDefinitions {
    private static final CropCatalog PENDING = new CropCatalog();
    private static final List<String> ERRORS = new ArrayList<>();
    private static Map<String, CropDefinition> resolved;

    static {
        PENDING.put(new CropDefinition("agricrops:wheat", "minecraft:wheat_seeds", "minecraft:wheat",
                "minecraft:farmland", "agricrops.crop.wheat", timeline(1200, 1200, 1200, 1200),
                StandardGenome.neutral(), 1, 1, 9, 15), false);
    }

    private CropDefinitions() {}

    public static GrowthTimeline timeline(int germination, int vegetative, int flowering, int fruiting) {
        return new GrowthTimeline(Arrays.asList(
                new GrowthTimeline.Segment(GrowthPhase.GERMINATION, germination),
                new GrowthTimeline.Segment(GrowthPhase.VEGETATIVE, vegetative),
                new GrowthTimeline.Segment(GrowthPhase.FLOWERING, flowering),
                new GrowthTimeline.Segment(GrowthPhase.FRUITING, fruiting)));
    }

    public static void submit(CropDefinition definition, boolean replace) { PENDING.put(definition, replace); }
    public static void error(String message) { ERRORS.add(message); }

    public static void freeze() {
        if (!ERRORS.isEmpty()) throw new IllegalStateException("Invalid Agricrops definitions: " + ERRORS);
        Map<String, CropDefinition> candidate = PENDING.freeze();
        for (CropDefinition definition : candidate.values()) {
            validateItem(definition.seed());
            validateItem(definition.product());
            if (!Block.REGISTRY.containsKey(new ResourceLocation(definition.soil()))) {
                throw new IllegalArgumentException(definition.id() + ": missing soil block " + definition.soil());
            }
        }
        resolved = Collections.unmodifiableMap(new LinkedHashMap<>(candidate));
    }

    private static void validateItem(String id) {
        Item item = Item.REGISTRY.getObject(new ResourceLocation(id));
        if (item == null || item == Items.AIR) throw new IllegalArgumentException("Missing crop item " + id);
    }

    public static Map<String, CropDefinition> all() {
        if (resolved == null) throw new IllegalStateException("Crop definitions are not frozen yet");
        return resolved;
    }
    public static CropDefinition get(String id) { return all().get(id); }
}
