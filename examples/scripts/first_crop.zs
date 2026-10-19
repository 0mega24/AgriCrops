// Optional first-slice test override. Copy into the instance scripts/ folder and restart.
// Still uses the existing wheat seed/product and vanilla wheat models; no art files needed.
mods.agricrops.Crops.builder("agricrops:wheat", "minecraft:wheat_seeds", "minecraft:wheat", "agricrops.crop.wheat")
    .soil("minecraft:farmland")
    .phaseTicks(1200, 1200, 1200, 1200)
    // Stored age IDs: 0 earliest, 1 default regrowth, 2..6 intermediate, 7 mature (invalid target).
    .regrowFrom(1)
    .costs(1, 1)
    .light(9, 15)
    .baseStats([0, 0, 0, 0, 0, 0, 0, 0])
    .replace();
