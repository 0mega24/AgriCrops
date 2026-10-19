package dev.omega24.agricrops.core.genetics;

/** Stable identifiers; persistence must use ids rather than enum ordinals. */
public enum StandardStat {
    GROWTH("growth", false),
    YIELD("yield", false),
    RESILIENCE("resilience", false),
    WATER_AFFINITY("water_affinity", false),
    FERTILITY_RESPONSE("fertility_response", false),
    LIGHT_TOLERANCE("light_tolerance", true),
    TEMPERATURE_TOLERANCE("temp_tolerance", true),
    MUTATION_CHANCE("mutation_chance", false);

    public static final int MAX = 63;
    private final String id;
    private final boolean directional;

    StandardStat(String id, boolean directional) {
        this.id = id;
        this.directional = directional;
    }

    public String id() { return id; }
    public boolean isDirectional() { return directional; }
    public int minimum() { return directional ? -MAX : 0; }

    public void validate(int value) {
        if (value < minimum() || value > MAX) {
            throw new IllegalArgumentException(id + " must be in " + minimum() + ".." + MAX);
        }
    }
}
