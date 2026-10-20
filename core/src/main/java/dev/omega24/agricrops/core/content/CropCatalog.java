package dev.omega24.agricrops.core.content;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Collect first, validate completely, then freeze. Late script mutations are unsupported. */
public final class CropCatalog {
    private final Map<String, CropDefinition> pending = new LinkedHashMap<>();
    private boolean frozen;

    public void put(CropDefinition definition, boolean replace) {
        if (frozen) throw new IllegalStateException("Definitions are frozen; restart after script edits");
        boolean exists = pending.containsKey(definition.id());
        if (exists != replace) {
            throw new IllegalArgumentException(replace ? "Cannot replace missing crop " + definition.id()
                    : "Duplicate crop " + definition.id() + "; use replace explicitly");
        }
        pending.put(definition.id(), definition);
    }

    public Map<String, CropDefinition> freeze() {
        if (frozen) throw new IllegalStateException("Definitions already frozen");
        Set<String> seeds = new HashSet<>();
        for (CropDefinition definition : pending.values()) {
            if (!seeds.add(definition.seed() + "@" + definition.rules().seedMeta)) throw new IllegalArgumentException("Ambiguous seed item/metadata " + definition.seed());
        }
        frozen = true;
        return Collections.unmodifiableMap(new LinkedHashMap<>(pending));
    }
}
