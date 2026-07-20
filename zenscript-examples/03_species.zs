// mods.agricrops.Species - registering a CropSpecies for a plant so it can participate in
// cross-breeding and attribute unlocks (crop-mod-design.md §7). By convention the species id
// must match the plant id it backs - see 04_plants.zs for the plant this backs.

import mods.agricrops.Species;

// Simple form (commented out - don't call both for the same id, the second call would just
// warn "already registered" and skip): "wild-type" (all-zero base stats), with one
// locked-attribute slot. Every baseline fork plant (wheat, carrot, cactus, ...) is
// registered this way in Java (DefaultSpecies.java) - this is the same thing, just from a
// script.
// Species.register("my_pack:frostberry", ["heat_resistance"]);

// Richer form: a custom base stat template instead of all-zero. Order matches CropStat's
// declaration order: growth, yield, resilience, water_affinity, fertility_response,
// light_tolerance, temp_tolerance, mutation_chance. Each is -63..63 (out-of-range values get
// clamped). This frostberry starts cold-hardy (temp_tolerance -30) and drought-resistant
// (water_affinity 0 = generalist), but grows slowly (growth -10) - see the genetics
// reference in crop-mod-packdev-guide.md for what each stat actually does.
Species.register("my_pack:frostberry", [-10, 15, 20, 0, 0, 0, -30, 0], ["heat_resistance"]);
