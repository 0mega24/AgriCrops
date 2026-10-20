# Small-content POC: gameplay and authoring

Implementation started 2026-10-20. See `poc-acceptance.md` for evidence, not just source presence.
This replaces the **first-crop-only** description, not the archived fork. Java 8 / Forge 1.12.2;
CraftTweaker is optional. There is no external simulator.

## Default content and player loop

Three parents: wheat (4 × 1200 active ticks), beetroot (4 × 1000), carrot (4 × 1300). Native seeds
are required; carrot is its own native seed. Crop sticks support their retained specimens.
Place on farmland, supply water/nutrients, probe conditions, harvest mature plants with an empty
hand. Harvest retains the line and rewinds to numeric stage 1 by default; CT accepts 0..6.
Biological phase windows are independent of eight render stages.

One extra set of sticks on an empty bed enables crossing. Two cardinally adjacent eligible
parents must overlap authored donor/recipient phases. Wheat donates/receives in flowering;
beetroot donates in flowering but receives in vegetative growth; carrot donates in fruiting and
receives in flowering. Stagger planting or otherwise synchronize the roles. One canonical pair
provides all genes, alleles and output composition. Each parent pays 100 water + 100 nutrients,
a 20-active-tick setback and a 200-world-tick cooldown. Rejected pairs do not debit sources.
Compatible same-family offspring retain the recipient body but can emit existing products from
either parent. Mixes are capped at four active crop definitions; this is not a new hybrid item
for every combination. Incompatible source permissions, surface soils and foundations reject.
The body controls soft physiology; every active product retains its hard environmental gates,
surface soil and foundations. A minority rare-source component cannot evade its habitat gate by
using an easier body. The probe identifies the blocked source/factor without changing its genes.

Eight bounded genes have consumers: Growth changes work rate; Yield changes the **one** harvest
unit budget; Resilience softens environmental penalties, not hard gates; WaterAffinity and
FertilityResponse alter response to actual accessible stocks; LightTolerance and TemperatureTolerance
shift preferred bands; MutationChance widens inheritance variation/regression. Breeding can
improve or regress; repeated parents are not a guaranteed upgrade path.

Wheat × beetroot can introduce Frost Hardiness, wheat × carrot Heat Hardiness (competing climate
locus), beetroot × carrot Deep Roots. Alleles segregate and dominance affects expression. Three
stable generations from distinct lineage tokens fix a homozygous locus; clones cannot advance
that counter. Fixed paths exclude competing alleles. Sneak-use bone meal on an empty crossing
bed toggles recovery: an actual fresh wild/imported/crafted donor reopens the offspring's fixed
locus at double propagation costs. This is not a player-wide permanent unlock.

Shears take a clipping from a vegetative plant, with the same paid propagation/cooldown/setback.
It retains genes, composition, alleles, lineage and sexual generation, but starts at zero growth
and with no paid production. It spends 400 eligible active ticks establishing roots before
growth/breeding. A clipping never counts as fresh wild ancestry. Crafted wheat foundation seeds
use wheat plus bone meal and start from the definition baseline, not their ingredient's genes.

Breaking returns a generic retained seed carrying specimen identity, **not** prepaid production
or soil resources. These generic carriers cannot be vanilla-planted and silently stripped of
genetics. Arbitrary other-mod recipes can still consume items; no universal recipe protection
is promised.

## Environment, soil and conservation

Plant genetics, live development, soil stocks and observations are separate. Temperature uses
vanilla's position-aware biome temperature once (no duplicate altitude penalty). Humidity is an
explicit game proxy: biome rainfall plus 0.1 exposed-rain adjustment, clamped 0..1. Air quality
is an authored profile, **not a pollution sensor**: baseline 1, desert .85, Nether .35. Actual
light, Y, dimension, sky exposure and rain are sampled. Dimension overrides apply before biome
overrides; explicit constants support authored extra factors. Missing required observations
fail closed. No invented season/weather/pollution integration.

Default preferred temperature bands: wheat .3..1.2, beetroot .1...9, carrot .5..1.5, tolerance 1.
Humidity .25...8/tolerance .8; air .65..1/tolerance .65; aeration .3..1/tolerance .4. Light 9..15
is a hard boundary with bounded inherited adaptation. The worst soft fit limits growth; dozens
of hidden bonuses do not multiply together. Resilience can soften, not erase, that tradeoff.

Up to three contiguous supported cells directly below a plant have **position-owned** stocks.
Germination reaches one, vegetative two, flowering onward three, capped by body/root traits.
Deeper water cannot feed a seedling without actual reach. Profiles exist for farmland, dirt,
grass, clay, sand and gravel. Capacities, evaporation, drainage, aeration, nutrient-release and
per-cell uptake differ. Unsupported blocks stop the root column. Authored foundations can
require a real block at depth 1..3 even before the roots reach it.

Newly observed/replaced blocks do not mint fertilizer. A bucket supplies 1000 water units,
bone meal 2000 available nutrients, compost 2000 slow-release reserve nutrients. Inputs never
skip stages. Rain adds 12 surface water per eligible interval. Nearby vanilla water supplies
8 per interval after a bounded 162-position scan cached for 100 world ticks. These are explicit
renewable external sources, not copied soil pools. Soil processes once per cell per interval;
downward transfer conserves water, bottom runoff/evaporation are losses. Organic release debits
the same reserve that becomes available nutrients. Profile changes do not refill stocks.

Field reservoirs hold 8000 mB water (1 mB = 1 soil water unit), 10000 available nutrients and
10000 compost reserve. Bucket/fertilizer/compost inputs and a Forge water-fluid capability feed
the same finite tank. Every 20 ticks, at most four of 25 loaded bed candidates within two blocks
receive up to 120 water/nutrients each, with a persisted round-robin cursor. No unloaded chunks
are force-loaded. Reservoir contents survive save/reload but are lost when the block is broken;
the empty block does not duplicate filled storage. Hopper item supply/general crop automation
are not implemented in this POC.

Each harvest plans a bounded output allocation and **full source-reference-cycle** resource
price before growth. Default two harvest units, plus Yield/16. Weighted allocation persists
credits so small harvests eventually include minority components. A late regrowth stage still
pays the full next production budget. Speed is not free yield. Saved funding/outputs/credits
prevent reload rerolls or repeated instant harvests. These prices and default timings are
deliberately provisional, not balanced survival pacing.

## In-game explanation and art

- Stick/sneak inspection: brief growth/reason/resource summary, not genes.
- Field probe: actual environment values and source labels, layer stocks, reach and limiting fit.
- Seed analyser on a planted crop: costs one paper; exposes exact genes/composition/alleles/fixed
  path status and records discoveries in player-persisted data. Creative inspection costs nothing.
- Field journal on crop sticks: public requirements/windows/foundations and practical actions;
  observed-parent research leads, then discovered trait crosses. POC presentation is localized
  chat text, not a polished scrolling research GUI. Long journals need pagination before breadth.
- Normal retained-seed tooltips do not dump hidden alleles. Item NBT is not cryptographic secrecy.
  Crop chunk/update packets contain only the render profile, never the genome.

Three crop visual profiles borrow existing vanilla wheat/beetroot/carrot stage models/textures.
The recipient body determines appearance, not each product fraction. Generic seed/clipping/tool
items and the oak-plank reservoir also borrow existing native art. **No new PNG is required for
this POC.** Custom art, alternate body models, mixed overlays and a polished reservoir skin are
future presentation work, not missing mandatory crop textures.

## Java-backed ZenScript

`examples/scripts/first_crop.zs` overrides wheat. `examples/scripts/poc_ecology.zs` demonstrates
real custom factors, biome overrides, soil/trait definitions and a gated tagged-item import.
Scripts run through actual CT, then definitions validate/freeze. Restart after editing;
`/ct reload` cannot mutate a live population. Duplicate IDs require an explicit replace; unknown
items/blocks/factors, invalid bounds or references fail startup rather than manufacture defaults.

Crop builder adds `.visual("wheat"|"beetroot"|"carrot")`, `.compatibility(id)`, `.roots(1..3)`,
`.harvestUnits(1..4)`, `.requirement(factor,min,max,tolerance,hard)`, `.foundation(depth,block)`,
`.windows(donorPhase,recipientPhase)`, `.propagation(clippable,breedable,rootTicks)`,
`.reproductionCosts(water,nutrients)`, `.loci(ids)`, `.itemMetadata(seedMeta,productMeta)` and
`.itemNbt(seedTemplate,productTemplate)`, alongside the original timing/cost/stat/regrowth fields.
The default builder has generic wheat-like environmental preferences; author them explicitly
for non-wheat species. Requirements with the same factor replace that body's earlier rule.

`mods.agricrops.Ecology` exposes `constantFactor`, `biomeFactor`, `dimensionFactor`, `soil`,
`trait`, `unlock`; see the example for signatures. Java integrations use the same immutable
rules/store. Trait effects are tolerance/cold/heat tolerance, root depth, water/nutrient demand.
Java addons may register bounded live observations through `WorldEnvironment.registerProvider`;
a null/failed observation stays unavailable and required plants wait. No real optional pollution
or season addon is bundled or qualified. Soil moisture/nutrition/aeration are always recomputed
from actual cells by the phenotype solver, not conjured by external observations.

Native imports use an **existing** source seed item, exact metadata and an optional partial NBT
template; output templates preserve explicit product metadata/NBT. One seed item/metadata pair
maps to one definition; ambiguous NBT-only variants are deliberately rejected. Source items
must already be acquired: this bridge does not create recipes or automatically grant gated
seeds. Authors choose crossability, family, requirements and a known fallback visual. Pam's or
Mystical Agriculture can use this explicit bridge with version-correct IDs and mod-loaded
script guards, but neither mod/version has been installed or qualified here. There is no
automatic catalogue importer or inferred imitation of their special native mechanics.

## Persistence and qualification limits

Schema 2 retains specimen genes/traits/products/origin/ancestry and separate growth/rooting,
paid production and allocation credits. Old schema-1 crops keep identity/progress, but must
pay any missing production budget before harvest; migration cannot invent paid reserves.
Schema-1 soils retain stocks with zero organic reserve. Unsupported/corrupt specimens remain
quarantined, not reset to healthy seeds. Keep backups; no promise of old-fork save compatibility.

Full POC action evidence, GUI/manual checks, optional integrations and clean-build evidence are
tracked separately in `poc-acceptance.md`. Do not expand defaults until those gates are clear.
