// mods.agricrops.Plants - defining a new crop (crop-mod-design.md §11 step 8). Replaces the
// dead AgriCore-JSON plant pipeline as the pack-dev-facing way to add a crop without a Java
// patch or new art. Nothing is registered until .register() is called at the end of the chain.

import mods.agricrops.Plants;
import crafttweaker.item.IItemStack;

Plants.builder("my_pack:frostberry", "Frostberry")
    .soil(["tilled"])
    // Reuses the fork's EXISTING carrot art (assets/agricrops/textures/{blocks,items}/
    // crop_carrot*.png, seed_carrot.png) - zero new PNGs, zero new blockstate/model JSON.
    // Point this at any already-vendored crop_<id>/seed_<id> pair, or another mod's texture
    // resource location, the same way.
    .textures("agricrops:blocks/crop_carrot", "agricrops:items/seed_carrot")
    .growthStages(4)
    .products([<minecraft:snowball>])
    .biome(["COLD"])
    .elevation(60, 120)
    // Staged loot (crop-mod-design.md §6): a partial-growth tier that does NOT reset growth
    // (repeat-harvestable, IC2-Crops style), plus the final full-growth tier that does.
    // WeightedItemStack (.percent(...)/.weight(...)) is CraftTweaker's own type - no
    // agricrops-specific loot-table syntax to learn.
    .lootTier(1, false, [<minecraft:snowball>.withAmount(1).percent(50.0)])
    .lootTier(3, true, [<minecraft:snowball>.withAmount(3).percent(100.0)])
    .register();
