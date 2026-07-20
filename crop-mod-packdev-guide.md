# Pack-dev guide — genetics reference and ZenScript authoring

Companion to `crop-mod-design.md`. That doc covers *why* the mechanics work the way they do;
this one is the pack-dev-facing reference for actually authoring content against them —
what the 8 genetic stats do, what's exclusive to this fork vs. inherited from AgriCraft, and
how to define crops/species/mutations without touching Java.

## 1. Two ways to add content — JSON is dead, don't use it

Upstream AgriCraft's JSON plant-definition pipeline (`AgriCore`'s `AgriPlant`/`AgriLoader`,
loaded from `config/agricrops/json/defaults/`) still exists in the codebase, but it's
vestigial: no JSON files ship with the mod, and `JsonPlant` doesn't implement *any* of this
fork's rewrite-era mechanics (soil composition matching is explicitly a no-op for JSON plants,
there's no biome/elevation support, no staged loot tiers). Don't author new content against it.

The two live paths are:

- **Java** (`CodePlant` + a registration class like `DefaultPlants.java`/`DefaultSpecies.java`)
  — for baseline/built-in content shipped with the mod itself.
- **ZenScript** (`mods.agricrops.*`, via CraftTweaker) — for everything else: packs, add-ons,
  and dev/test content you want to iterate on without recompiling. This is what the rest of
  this guide covers.

ZenScript requires [CraftTweaker](https://www.curseforge.com/minecraft/mc-mods/crafttweaker)
installed alongside Agricrops (a soft dependency — the mod loads fine without it, you just
won't have `mods.agricrops.*` available). Scripts live in `<instance>/scripts/*.zs`; see
`zenscript-examples/` in this repo for full worked examples of everything below, plus a setup
walkthrough.

**Ordering note**: unlike CraftTweaker's own recipe-manipulation calls, `mods.agricrops.*`
registrations are **not** deferred through CraftTweaker's `IAction` queue — they take effect
immediately, in the order your script(s) call them. This is deliberate: agricrops' registries
have no Forge-recipe-load-order conflict to defer around, and deferring them anyway would
silently break cross-registry dependencies (e.g. a plant referencing a soil composition that
was "registered" in a different file, but not yet actually applied because it was sitting in
the same deferred queue). Practical upshot: register soil compositions before the soil
profiles that use them, attributes before species that slot them, species before plants that
back them, in whatever order you actually write the calls — CraftTweaker loads `.zs` files
alphabetically by default, hence the `01_`/`02_`/... prefixes in `zenscript-examples/`.

## 2. The `mods.agricrops.*` API

### `mods.agricrops.Soils` (crop-mod-design.md §3)

- `addComposition(id, name)` — registers a new soil composition tag. The 8 built-in tags
  (`dirt`, `grass`, `tilled`, `sand`, `gravel`, `clay`, `stone`, `soul_sand`) already exist;
  you only need this for a genuinely new tag.
- `addProfile(blockRegistryName, compositionId, nutrientCapacity, moistureRetention, growthMultiplier)`
  — gives a block (e.g. `"minecraft:mycelium"`) a growth-mechanics profile. `moistureRetention`
  is 0..1 (1.0 = never decays, 0.0 = decays fully every tick); `growthMultiplier` is a flat
  growth-chance multiplier for that surface.

### `mods.agricrops.Attributes` (crop-mod-design.md §7)

- `addAttribute(id, bipolar)` / `addAttribute(id, bipolar, stabilizationThreshold)` — registers
  a new locked attribute. `stabilizationThreshold` defaults to 3 ("generations bred true"
  before it stops risking drop-out on the next cross).
- `addUnlockRule(speciesA, speciesB, attributeId, chance)` — crossing these two species gets a
  `chance`-weighted (0.0..1.0) roll to unlock `attributeId`, if neither parent already carries
  it. Both species need a matching `lockedAttributeSlot` (see `Species`, below) or the rule is
  inert.
- `markIncompatible(speciesA, speciesB)` — a hard block: these two can never cross at all,
  distinct from simply having no unlock rule (which still allows a plain cross).

### `mods.agricrops.Species` (crop-mod-design.md §7)

- `register(plantId, lockedAttributeIds)` — "wild-type" species: all-zero base stats, with
  the given locked-attribute slots.
- `register(plantId, baseStats, lockedAttributeIds)` — same, but with a custom base stat
  template. `baseStats` is an 8-element int array in `CropStat` declaration order (see §3
  below) — the default roll a fresh, unbred genome of this species gets for each stat.

By convention a species id **must match** the plant id it backs (one species per plant) — the
registries aren't cross-referenced any other way.

### `mods.agricrops.Mutations` (crop-mod-design.md §6/§7)

- `addMutation(id, chance, childId, parentIds)` — the actual "these parents in adjacent crop
  sticks can produce this child" relation. This is a *separate* registration from attribute
  unlock rules — a cross needs both a mutation entry (or it can't happen) and, independently,
  whatever unlock rules apply to whether it also grants a locked attribute.

### `mods.agricrops.Plants` / `PlantBuilder` (crop-mod-design.md §11 step 8)

`Plants.builder(id, name)` returns a fluent builder; nothing is registered until you call
`.register()`:

- `.soil(String[])` — composition tags this plant can grow on. Empty/unset = any registered
  soil is acceptable.
- `.growthStages(int)` — default 4.
- `.renderMethod(String)` — `CROSSED` (default, X-shape), `HASHTAG` (vanilla-wheat-style),
  `STEM` (vine + fruit cube, melon/pumpkin-style), `TALL_CROSSED`, `TALL_HASHTAG`.
- `.textures(textureBase, seedTexture)` — **reuse existing art, zero new PNGs**: `textureBase`
  is a resource location up to the stage number (e.g. `"agricrops:blocks/crop_carrot"` resolves
  `crop_carrot1.png`..`crop_carrot4.png`); `seedTexture` is the full seed-icon resource
  location (e.g. `"agricrops:items/seed_carrot"`). Point these at any already-vendored
  `crop_<id>`/`seed_<id>` pair (or another mod's textures entirely) to skin a new plant with
  existing art. No blockstate/model JSON is ever needed — growth-stage textures resolve
  dynamically at render time, not through vanilla blockstate variants.
- `.products(IItemStack[])` — default harvest drop(s), used if no loot tiers are added.
- `.unverifiedSeeds(IItemStack[])` — vanilla/other-mod items plantable directly as an
  "unverified" (unanalyzed) seed for this plant, same as upstream AgriCraft's seed-wrapper
  behavior. The mod's own analyzed seed item is always accepted regardless.
- `.biome(String[])` / `.elevation(min, max)` — habitat preference (crop-mod-design.md §5).
  Empty biome list / 0..255 elevation = generalist (no preference).
- `.lootTier(minStage, resetsGrowth, WeightedItemStack[])` — staged loot (crop-mod-design.md
  §6). Call multiple times for multiple tiers; the highest `minStage` not exceeding the crop's
  current stage wins at harvest. Uses CraftTweaker's own `WeightedItemStack` type (build one
  with `someItem.percent(25.0)` or `someItem.weight(1.0)`) — no agricrops-specific loot-table
  syntax to learn. If never called, falls back to the single-tier-at-max-stage default (same
  as every plant registered before this feature existed).
- `.weed(boolean)` / `.aggressive(boolean)` — see "Weeds," below.

## 3. Genetics reference — the 8 stats

Every plant's genome is 8 signed bytes, range **-63..63**, baseline **0**. `CropStat.java` is
the fixed enum; `LockedAttribute`s (registered via `mods.agricrops.Attributes`) share the same
range/storage but are open-population, not fixed.

| Stat | Kind | Mechanical effect |
|---|---|---|
| Growth | magnitude | Tick-rate multiplier toward the next growth stage. |
| Yield | magnitude | Extra drops on harvest (scales roll count/quality *within* whichever loot tier was hit — doesn't change which tier you get). |
| Resilience | magnitude | Resistance to bad biome/elevation/soil, trampling, **and weed risk** — see below. |
| Water Affinity | magnitude, bidirectional pull | Swings growth chance up when moist, down when dry, around a neutral baseline — 0 is a moisture generalist, unaffected either way (not "bad," unlike most magnitude stats). |
| Fertility Response | magnitude | How efficiently the plant converts nutrient-pool contents into growth chance. |
| Light Tolerance | **bipolar** | Negative = shade-loving, positive = wants full sun. 0 isn't a worse version of either — it's a third, generalist archetype. |
| Temp Tolerance | **bipolar** | How far from biome-normal temperature the plant tolerates before a penalty. Same archetype-not-axis framing as Light Tolerance. |
| Mutation Chance | magnitude | Chance to donate stats when cross-breeding/spreading, and (see below) a weed-risk input. |

**Magnitude vs. bipolar is the one distinction that actually matters for balancing**: for a
magnitude stat, "higher is generically better at that one thing" (a naive `max(value, 0)`
scoring approach works). For a bipolar stat, that same scoring silently breaks — a -50
Light Tolerance shade-plant isn't a "bad" +50 sun-plant, it's a different, equally valid build.
Only Light Tolerance and Temp Tolerance are bipolar today.

### The one explicit tradeoff that exists today: weed risk

`weedRisk = f(Growth, Mutation Chance) − Resilience`. A plant bred purely for speed/mutation
without also investing in Resilience is a ticking time bomb — eventually it destabilizes (a
telegraphed window you can still cure by right-clicking) and, if left uncured, converts into a
weed. This is the *only* stat-combination tradeoff currently implemented anywhere in the mod.

### What's *not* currently coupled: Growth speed vs. resource cost

A natural extension — "slow growth + high yield should be cheaper/more stable to run than
max growth + max yield, since it needs less water/fertilizer per unit time" — is **not**
currently modeled. Today, resource drain is flat per successful growth tick regardless of the
Growth stat's magnitude:

- **Nutrients**: a fixed 1 unit drained per successful growth tick (`GROWTH_TICK_NUTRIENT_DRAIN`
  in `TileEntityCrop.java`), refilled by fertilizer (20 units per application — a nutrient-pool
  deposit, not an instant vanilla-bonemeal-style boost).
- **Water**: decays a fixed 2 units every growth tick regardless of what else happens that tick,
  topped up 25 units if a water source is within range, softened by the soil's moisture
  retention (clay retains almost fully, sand barely retains at all). This *is* the
  "usage-vs-refill" model — moisture is a depleting pool you have to keep earning back, not a
  one-shot fill.

Since a high-Growth plant does succeed more ticks per unit wall-clock time, it *does* burn
through nutrients/moisture faster in practice — but there's no explicit per-tick cost
multiplier tied to the Growth stat itself, so "fast growth costs more" is an emergent side
effect today, not a legible, tunable design lever. Making that coupling explicit (e.g. scaling
`GROWTH_TICK_NUTRIENT_DRAIN`/moisture decay by the plant's own Growth stat in
`CropStatCalculator`) is an open balance decision, not yet implemented — flagging it here
rather than silently assuming it already works this way.

## 4. Cross-breeding and weeds, tied together

- **Mutations** (`mods.agricrops.Mutations`) define which parent plants can produce which
  child at all.
- **Species + Attributes** (`mods.agricrops.Species`/`Attributes`) layer on top: whether that
  same cross *also* has a chance to unlock a new locked attribute, and whether two species are
  hard-incompatible regardless of any mutation entry.
- **Weeds** are a separate axis entirely from both of the above. The built-in weed-risk/
  destabilizing mechanic (driven purely by a crop's own Growth/Mutation Chance vs. Resilience)
  only ever converts an existing crop into the one hardcoded `agricrops:weed` plant — it's not
  something you register content against. If you want your **own** standalone weed-like plant
  (rake-only harvest, optionally spreading into neighbors), use `Plants.builder(...).weed(true)`
  (and `.aggressive(true)` for the spreading behavior) — see `zenscript-examples/06_weed.zs`.
  A pack-defined weed this way is deliberately never given a `CropSpecies` — weeds don't
  participate in cross-breeding, matching the built-in weed's own treatment.

## 5. Dev-testing tips

- Getting a seed in-game requires **no extra registration**: `CodePlant`'s constructor always
  adds the mod's own analyzed-seed item to a plant's seed list, so anything added to the plant
  registry (Java or ZenScript) immediately shows up in the creative tab/JEI with a correctly
  textured, working seed.
- `/ct reload` re-runs every script without a full relaunch — much faster than
  recompile-and-relaunch for iterating on stat numbers, loot tiers, and soil profiles. Watch
  the log for `agricrops:` lines; every `mods.agricrops.*` call logs what it did (or why it was
  skipped — e.g. an id that's already registered, or a plant missing required `.textures(...)`/
  `.products(...)` calls).
- Reusing existing art (`.textures(...)` pointing at an already-vendored `crop_<id>`/
  `seed_<id>` pair) is the fastest way to get a new test plant fully rendering with zero asset
  work — see §2 above.
