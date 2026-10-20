package dev.omega24.agricrops.core.environment;

import dev.omega24.agricrops.core.content.CropDefinition;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable observations. A missing channel is unavailable, never an invented ideal reading. */
public final class Environment {
    private final Map<String, Double> values;
    private final Map<String, String> sources;
    public Environment(Map<String, Double> values, Map<String, String> sources) {
        if (values.size() > 32 || !values.keySet().equals(sources.keySet())) throw new IllegalArgumentException("Invalid environment snapshot");
        Map<String, Double> copy = new LinkedHashMap<>();
        values.forEach((id, value) -> {
            CropDefinition.identifier(id);
            if (value == null || !Double.isFinite(value) || Math.abs(value) > 1000000) throw new IllegalArgumentException("Invalid factor " + id);
            if (sources.get(id) == null || sources.get(id).length() > 256) throw new IllegalArgumentException("Missing factor source");
            copy.put(id, value);
        });
        this.values = Collections.unmodifiableMap(copy);
        this.sources = Collections.unmodifiableMap(new LinkedHashMap<>(sources));
    }
    public Map<String, Double> values() { return values; }
    public boolean has(String id) { return values.containsKey(id); }
    public double get(String id) { if (!has(id)) throw new IllegalArgumentException("Unavailable factor " + id); return values.get(id); }
    public String source(String id) { return sources.get(id); }
    public Environment with(String id, double value, String source) {
        Map<String, Double> next = new LinkedHashMap<>(values); next.put(id, value);
        Map<String, String> provenance = new LinkedHashMap<>(sources); provenance.put(id, source);
        return new Environment(next, provenance);
    }
    public static Environment reference(int light) {
        Map<String, Double> values = new LinkedHashMap<>(); Map<String, String> sources = new LinkedHashMap<>();
        String[] ids = {"light", "temperature", "humidity", "air_quality", "height", "moisture", "nutrition", "aeration", "rain", "sky", "dimension"};
        double[] numbers = {light, 0.8, 0.5, 1, 64, 0.5, 0.5, 1, 0, 1, 0};
        for (int i = 0; i < ids.length; i++) { values.put("agricrops:" + ids[i], numbers[i]); sources.put("agricrops:" + ids[i], "reference fixture"); }
        return new Environment(values, sources);
    }
}
