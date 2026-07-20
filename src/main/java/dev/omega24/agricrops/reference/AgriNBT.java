/*
 * 
 */
package dev.omega24.agricrops.reference;

//NBT tags
/**
 * Class for tracking AgriCraftNBT names.
 *
 * <s>This is fine, as these are never supposed to change, across all classes.</s>
 *
 * @since 2.0.0
 */
public interface AgriNBT {

    /**
     * Genome NBT contract (crop-mod-design.md §10's "standardize genome data in item NBT"):
     * every seed/clipping item ({@code ItemAgriSeed}, {@code ItemClipping}) carries exactly
     * these tags on its stack NBT, and nothing else defines genome data:
     * <ul>
     * <li>{@link #SEED} - the species/plant id (String).
     * <li>{@code PlantStats.NBT_STATS} ("agri_stats") - the widened stat byte array.
     * <li>{@code PlantStats.NBT_ANALYZED} ("agri_analyzed") - whether the genome has been
     * revealed to the player (boolean).
     * <li>{@code PlantStats.NBT_LOCKED_ATTRIBUTES} ("agri_locked_attributes") - unlocked
     * {@code LockedAttributeInstance}s (NBT list), absent entries meaning "never unlocked."
     * </ul>
     * This is already consistent across both item types (no rework needed for §11 step 8) -
     * it's what lets OC's Database Upgrade/item filters and similar compat mods key off
     * species/traits without bespoke per-mod integration, as long as they read/write these
     * exact tag names rather than reinventing their own.
     */
    String CROSS_CROP = "agri_cross_crop";
    String VINE = "agri_vine";
    String AXIS = "agri_axis";
    String DISCOVERED_SEEDS = "agri_discovered";
    String LEVEL = "agri_level";
    String ID = "agri_id";
    String META = "agri_meta";
    String MATERIAL = "agri_material";
    String MATERIAL_META = "agri_material_meta";
    String ACTIVE = "agri_sprinkled";
    String POWER = "agri_power";
    String INVENTORY = "agri_inventory";
    String COUNT = "agri_count";
    String SEED = "agri_seed";
    String X = "agri_X";
    String Y = "agri_Y";
    String Z = "agri_Z";
    String FLAG = "agri_flag";
    String DIRECTION = "agri_direction";
    String MULTI_BLOCK = "agri_multiblock";
    String BUFFER = "agri_buffer";
    String COLUMN_COUNTER = "agri_column_counter";
    String WATER_USAGE_REMAINING_MB = "agri_water_usage_remaining_mb";
    String WATER_USAGE_REMAINING_TICKS = "agri_water_usage_remaining_ticks";

    String FLUID_AMOUNT = "agri_fluid_amount";

    String DESTABILIZING = "agri_destabilizing";
    String DESTABILIZE_EXPIRY = "agri_destabilize_expiry";

    String AUTO_FERTILIZE = "agri_auto_fertilize";
    String AUTO_WATER = "agri_auto_water";

    String SCANNER_RADIUS_XZ = "agri_scanner_radius_xz";
    String SCANNER_RADIUS_Y = "agri_scanner_radius_y";

}
