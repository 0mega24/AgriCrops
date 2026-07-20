// mods.agricrops.Soils - registering a new soil composition tag and giving a block a
// growth-mechanics profile (crop-mod-design.md §3). Every existing agricrops soil profile
// (dirt/grass/tilled/sand/gravel/clay/stone/soul_sand) is registered the same fork-native
// Java way this script does it - see DefaultSoilProfiles.java if you want to compare.

import mods.agricrops.Soils;

// A composition tag is just a label plants can require via .soil([...]) - "peat" isn't
// built in, so if you want a plant that only grows on it, register the tag first.
Soils.addComposition("peat", "Peat");

// Give mycelium a profile using that tag: nutrient capacity, moisture retention (0..1,
// 1.0 = never decays), growth multiplier. Mycelium has no profile out of the box, so this
// is a genuinely new soil surface, not overriding an existing one.
Soils.addProfile("minecraft:mycelium", "peat", 120, 0.8, 1.0);
