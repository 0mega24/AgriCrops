// mods.agricrops.Plants - a pack-defined weed variant (crop-mod-design.md §8). This is
// SEPARATE from the fork's built-in weed-risk/destabilizing mechanic, which only ever
// converts an existing crop into the one hardcoded agricrops:weed plant when its Growth/
// Mutation Chance stats outrun Resilience. .weed(true) here just lets you register your OWN
// standalone plant that behaves like a weed (rake-only harvest, and if also .aggressive(true),
// spreads into/overtakes neighboring crops) - it never participates in the destabilizing
// conversion itself, and isn't given a CropSpecies (weeds don't cross-breed).

import mods.agricrops.Plants;
import crafttweaker.item.IItemStack;

Plants.builder("my_pack:bramble", "Bramble")
    .soil([]) // empty = any registered soil is acceptable
    .textures("agricrops:blocks/crop_carrot", "agricrops:items/seed_carrot")
    .growthStages(4)
    .products([<minecraft:stick>])
    .weed(true)
    .aggressive(true)
    .register();
