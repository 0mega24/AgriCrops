# ZenScript examples

These `.zs` files are **documentation, not mod content** — they are not bundled into the mod
jar and CraftTweaker will not find them here. To actually use one:

1. Install [CraftTweaker](https://www.curseforge.com/minecraft/mc-mods/crafttweaker) (the
   1.12.2-era "CraftTweaker2" line — same one this mod's `compat/crafttweaker` package targets)
   alongside Agricrops.
2. Copy whichever files you want into `<instance>/scripts/` (CraftTweaker's own convention —
   it recursively loads every `.zs` file it finds there, alphabetically by default).
3. Launch (or run `/ct reload` in an already-running world) and check the log/`/ct` command
   output for `agricrops:` lines — every registration in `compat/crafttweaker` logs what it
   did via `CraftTweakerAPI.logInfo`.

Read them in numeric order — `03_species.zs` and `04_plants.zs` both reference
`my_pack:frostberry`, so `02`/`03` need to run before `04`/`05` for the ids they define to
already exist. There's no cross-file dependency ordering magic here: every `mods.agricrops.*`
call takes effect immediately when the interpreter reaches it (see
`crop-mod-packdev-guide.md`'s note on why these are deliberately *not* wrapped in
CraftTweaker's deferred `IAction` pattern the way vanilla recipe scripts are), so script/file
load order is registration order, same as reading top to bottom.

See `crop-mod-packdev-guide.md` (repo root) for the full `mods.agricrops.*` API reference, the
8-stat genetics writeup, and what's still Java-only.

| File | Demonstrates |
|---|---|
| `01_soils.zs` | New soil composition tag + profile on a previously-unregistered block |
| `02_attributes.zs` | Locked attribute + cross-breed unlock rule + hard incompatibility |
| `03_species.zs` | `CropSpecies` registration, wild-type and custom-base-stat forms |
| `04_plants.zs` | New crop reusing an existing texture set, with staged loot tiers |
| `05_mutations.zs` | The actual parent→child cross-breed relation |
| `06_weed.zs` | A pack-defined standalone weed/aggressive plant |
