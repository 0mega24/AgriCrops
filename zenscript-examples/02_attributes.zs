// mods.agricrops.Attributes - locked attributes and cross-breeding unlock rules
// (crop-mod-design.md §7). Attributes are an open-population registry (unlike the fixed
// 8-stat CropStat enum) precisely so packs can add their own via ZenScript instead of
// needing a Java patch - see LockedAttribute.java's own javadoc.

import mods.agricrops.Attributes;

// bipolar=true means plus/minus are two different valid directions (like Light/Temp
// Tolerance), not a good/bad axis. bipolar=false means "more is generically better."
Attributes.addAttribute("heat_resistance", true);

// Crossing wheat and cactus gets a 15% roll to unlock heat_resistance on the offspring -
// but only if BOTH species have a lockedAttributeSlot for it (see 03_species.zs).
Attributes.addUnlockRule("wheat", "cactus", "heat_resistance", 0.15);

// A hard block: these two species can never cross, no roll, no exceptions - distinct from
// simply having no unlock rule (which still lets a plain, un-attributed cross happen).
Attributes.markIncompatible("wheat", "nether_wart");
