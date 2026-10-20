// Optional qualification content, not a fourth bundled default. Restart after editing.
mods.agricrops.Ecology.constantFactor("poc:greenhouse", 0.0, 1.0, "authored shelter index", 0.75, false);
mods.agricrops.Ecology.biomeFactor("minecraft:plains", "agricrops:air_quality", 0.90);
mods.agricrops.Ecology.soil("minecraft:farmland", 10000, 10000, 1, 12, 10, 900, 0.9, true);
mods.agricrops.Ecology.trait("poc:water_saver", "agricrops:moisture", "water_demand", "agricrops:moisture", -0.25, 1, 3, "poc.trait.water_saver", false);
mods.agricrops.Crops.builder("poc:tagged_melon", "minecraft:melon_seeds", "minecraft:melon", "poc.crop.tagged_melon")
    .visual("carrot")
    .phaseTicks(200, 200, 200, 200)
    .roots(2)
    .harvestUnits(1)
    .itemMetadata(0, 0)
    .itemNbt("{poc_source:1}", "{poc_product:1}")
    .requirement("poc:greenhouse", 0.5, 1.0, 0.0, true)
    .foundation(2, "minecraft:dirt")
    .windows("vegetative", "flowering")
    .propagation(true, false, 200)
    .register();
