package dev.omega24.agricrops.api.v1.stat;

/**
 * The fork-native, widened stat set (crop-mod-design.md §2), replacing AgriCraft's flat
 * growth/gain/strength model. Every stat is stored as a signed byte in
 * {@link #MIN}..{@link #MAX} (-63..63).
 *
 * <p>{@link #isBipolar()} distinguishes how a stat's sign should be read:
 * <ul>
 * <li><b>Magnitude-only</b> ({@code bipolar == false}): 0 is wild-type/no effect, positive is
 * strictly better, negative is strictly a penalty.</li>
 * <li><b>Bipolar/directional</b> ({@code bipolar == true}): 0 is a generalist with no strong
 * bonus either way; the sign encodes which archetype the plant leans toward (e.g. shade vs.
 * sun), and magnitude encodes how strongly. Neither sign is "worse" than the other.</li>
 * </ul>
 * Aggregation code (e.g. {@link dev.omega24.agricrops.farming.CropStatCalculator}) must branch
 * on this flag rather than assuming a single {@code max(value, 0)} scoring rule for every stat.
 */
public enum CropStat {

    GROWTH("growth", false),
    YIELD("yield", false),
    RESILIENCE("resilience", false),
    WATER_AFFINITY("water_affinity", false),
    FERTILITY_RESPONSE("fertility_response", false),
    LIGHT_TOLERANCE("light_tolerance", true),
    TEMP_TOLERANCE("temp_tolerance", true),
    MUTATION_CHANCE("mutation_chance", false);

    public static final byte MIN = -63;
    public static final byte MAX = 63;

    private final String id;
    private final boolean bipolar;

    CropStat(String id, boolean bipolar) {
        this.id = id;
        this.bipolar = bipolar;
    }

    /**
     * The stat's stable id, used for NBT keys and translation keys.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @see CropStat class doc for what bipolar vs. magnitude-only means for aggregation.
     */
    public boolean isBipolar() {
        return this.bipolar;
    }

    public String getTranslationKey() {
        return "agricrops_tooltip." + this.id;
    }

    public static byte clamp(int value) {
        return (byte) Math.max(MIN, Math.min(MAX, value));
    }

}
