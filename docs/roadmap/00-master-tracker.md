# Agricrops master tracker

> Scope revision, 2026-10-19: Decision 0002 and the Agricrops vault contracts supersede old
> simulator requirements. New required design includes Java/CT imports/native art, regression/
> lockout/recovery, parent timing/clippings/seed origins, layers and localized in-game information
> depth. Tables below remain a dated prototype inventory. Retained 2026-10-01 XML proves 13 passing
> Java tests while current source has 14 methods; the historical 14-passed claim below is not
> substantiated by those files. No fresh heavy/runtime checks were performed in the design audit.

Last audit: 2026-10-01  
Audited revision: `27114412`  
Build result: `./gradlew test` succeeds with 14 deterministic/resource tests. A 2026-10-01
`./gradlew runServer` smoke check registered the built-in hybrid and reached ready state.  
Design basis: repository baseline written in `docs/product-design.md`; owner acceptance remains.
Older source comments still reference the missing `crop-mod-design.md`.

## Product statement

Agricrops is intended to become a standalone Minecraft Forge 1.12.2 crop-breeding and
field-management mod with inheritable genomes, environmental and resource pressures,
discoverable cross-species traits, staged production, dangerous weeds, player-facing research
tools, and optional computer automation. Packs can extend or replace built-in content without
Java, but the base jar must provide a coherent progression without CraftTweaker.

## Correction to earlier summaries

The following language is too strong and must not be used as a completion claim:

- **“Added nine minimal vanilla-equivalent plants.”** More precisely: source registers nine
  baseline `CodePlant` definitions plus a conversion-only weed. They are placeholders, have no
  shipped mutation progression, and do not demonstrate the planned genetics game.
- **“Replaced Growth/Gain/Strength with an eight-stat genome.”** More precisely: NBT and APIs now
  store eight standard values, but not all values affect gameplay, species base stats are not
  applied to fresh seeds, and inheritance still derives from upstream algorithms.
- **“Added soil, moisture, habitat, species, attributes, loot tiers, and crossbreeding.”** More
  precisely: portions of each system exist. Several exposed values are unused, several systems
  are unreachable without external scripts, and the base installation has no mutation graph.

## Stage dashboard

| Stage | Status | Exit condition | Depends on |
|---|---|---|---|
| 1. Product definition | Functional | Owner accepts repository design baseline and Decision 0001 | Owner acceptance |
| 2. Foundation/baseline | Partial | Clean, reproducible client/server baseline with honest content | Stage 1 |
| 3. Genome/inheritance | Partial | Every genome field has defined creation, inheritance, and tests | Stage 1 |
| 4. Soil/resources | Partial | Soil values are live, persistent, replaceable, visible, and balanced | Stages 2–3 |
| 5. Environment | Source only | Biome/elevation/light/temp all participate consistently | Stages 3–4 |
| 6. Breeding/progression | Partial | A shipped, discoverable mutation path is playable end-to-end | Stages 3–5 |
| 7. Harvest/production | Partial | Stages, yield, costs, and products form a balanced loop | Stages 3–6 |
| 8. Instability/weeds | Partial | Risk, telegraph, cures, spread, and recovery are fair and tested | Stages 3–7 |
| 9. Knowledge/tools | Partial | Mechanics are understandable without source or OC | Stages 3–8 |
| 10. Automation | Source only | Sensors and actuators can manage a field with bounded power/cost | Stages 4–9 |
| 11. Pack authoring/content | Partial | Reload-safe API can express and validate the complete system | Stages 3–10 |
| 12. Release quality | Partial | Tests, migration, performance, docs, client QA, release artifact | All |

## Work already performed

This is an inventory, not a completion list.

| Commit | Work represented | Current interpretation |
|---|---|---|
| `77810cfe` | Rename packages and mod identity | Broad mechanical hard-fork rename; legacy names remain in user-facing code/docs. |
| `36fa99b3` | Nix shell, CI, build notes | Build tooling added. |
| `d9c9a900` | AgriCore namespace correction | Repair after broad rename. |
| `f55715dc` | RetroFuturaGradle migration | Obsolete ForgeGradle/QuikMod stack replaced. |
| `cd399331` | Build repair | Compilation restored against RFG. |
| `901a9584` | Runtime reflections repair | Headless load blocker removed. |
| `57656a26` | Server verification note | Headless server reportedly reached ready state. |
| `e097d263` | Crop-stick placement/nugget gating | Regression repair and orphan-item gating. |
| `b176d870` | Remove upstream irrigation | Feature removal; no complete replacement currently exists. |
| `0ff47ecf` | Farmland soil matching | Block-identity soil matching proof. |
| `b776ff7e` | Baseline plant definitions | Nine placeholder definitions and assets reused; no built-in progression. |
| `540c095b` | Eight-value stat storage/API | Data migration is partial; semantics are incomplete. |
| `5a63b235` | Farmland and vanilla seed fixes | Reachability repair. |
| `1c787d0b` | Soil columns/moisture/habitat | Persistent data and some growth factors; unused/stale fields remain. |
| `84ca028c` | Species/attribute registries | Data model and demonstration registrations. |
| `907fb9d7` | Attribute roll/incompatibility hooks | Hooked into two-parent mutation path; semantics incomplete. |
| `0021d1ad` | Staged loot | Reachable staged harvest mechanism; content and balance sparse. |
| `5d5fe5f4` | Instability/weeds | Reachable risk, cure, conversion, and spread proof-of-concept. |
| `11f3a06f` | OC/scanner/discovery | Telemetry-heavy integration; scanner/discovery/actuation incomplete. |
| `27114412` | CraftTweaker authoring | Broad registration API and examples; reload and full-system expression incomplete. |

## Subsystem truth table

| Subsystem | Source exists | Reachable normally | Meaningful effect | Survival UX/content | Tests | Status |
|---|---:|---:|---:|---:|---:|---|
| Crop sticks/basic planting | Yes | Yes | Yes | Partial | No | Functional |
| Ten baseline/hybrid definitions | Yes | Yes | Limited | Partial | Resource test | Partial |
| Eight standard stat storage | Yes | Yes | All eight have a consumer | Analyzer only | Yes | Partial |
| Species base templates | Yes | Yes | Fresh genomes use template | Analyzer | Yes | Functional |
| Locked attributes | Yes | Built-in mutation | Frost hardiness affects cold growth | Analyzer text | Yes | Partial |
| Attribute slots | Yes | Child slot enforced | Prevents invalid trait storage | No | Yes | Partial |
| Dominance/alleles | Yes | Built-in mutation | Expression/inheritance | Poor | Yes | Partial |
| Stabilization/dropout | Yes | Built-in mutation descendants | 50% unstable single-carrier dropout | Poor | Yes | Partial |
| Soil composition/profile | Yes | Yes | Composition/capacity/retention | Poor | No | Partial |
| Soil growth multiplier | Yes | Yes as data | No | No | No | Source only |
| Nutrient pool | Yes | Yes | Growth factor/drain/refill | Poor | No | Partial |
| Moisture pool | Yes | Yes | Growth factor/decay/refill | Poor | No | Partial |
| Biome/elevation | Yes | Selected plants | Growth penalty | Poor | No | Partial |
| Light tolerance | Stored | Yes | Shifts preferred light | No | Yes | Partial |
| Temperature tolerance | Stored | Yes | Shifts preferred biome temperature | No | Yes | Partial |
| Mutation Chance | Stored | Yes | Weed risk only | Misleading | No | Partial |
| Built-in mutation graph | Yes | Yes | Wheat + Beetroot → Winter Wheat | Journal/JEI path | Registry test | Partial |
| Same-species spreading | Yes | Yes | Inherited stat changes | Legacy UX | No | Partial |
| Partial-stage loot | Yes | Script dependent | Yes | Sparse | No | Partial |
| Instability/weed conversion | Yes | Genetically gated | Yes | Particle/right-click only | No | Partial |
| Journal discovery | Yes | Yes | Species record | Legacy | No | Partial |
| Mutation discovery | No | No | No | No | No | Not started |
| Crop Scanner | Yes | OC only | Read-only field scan | No recipe/GUI | No | Partial |
| Crop-stick OC telemetry | Yes | OC only | Read-only plus signals | External-mod UX | No | Partial |
| Auto-water/fertilize flags | Yes | OC only | No actuator consumes them | No | No | Source only |
| CraftTweaker definitions | Yes | With dependency/scripts | Adds registrations | Examples not shipped | No | Partial |
| CraftTweaker reload/update | No replacement lifecycle | Misleading `/ct reload` guidance | Existing IDs remain | Poor | No | Not started |
| JSON compatibility path | Legacy source | Config dependent | Missing rewrite mechanics | Deprecated | No | Partial/deprecated |

## Cross-stage blockers and decisions

- [x] Replace the missing design with `docs/product-design.md`; retain the existing pack-dev guide
  as the current authoring reference.
- [x] Choose the primary product: standalone content mod with an optional authoring layer.
- [ ] Decide save compatibility expectations for pre-fork AgriCraft worlds and current 0.1.0
  worlds.
- [ ] Decide whether CraftTweaker is the long-term primary data language or whether a validated
  native data format should replace/supplement it.
- [ ] Define whether OpenComputers is optional enhancement or a core gameplay dependency.
- [ ] Define concrete meanings for every standard and locked stat before balancing numbers.
- [ ] Decide the replacement for removed irrigation and the energy/resource cost of automation.
- [ ] Establish a representative built-in crop and mutation set.
- [ ] Accept or amend Decision 0001 and `docs/product-design.md` as the owner-approved source of
  truth.
- [ ] Stabilize the Java/CT definition contract with in-game validation/import/asset diagnostics.
- [ ] Implement expanded phase-specific breeding, regression/lockout/recovery, clippings/origins,
  layered soil and localized progressive in-game documentation per vault contracts.

## Immediate recommended milestone

Build one deliberately small but complete vertical slice before adding more registries:

- three parent crops and one discoverable hybrid;
- nonzero species base templates applied to every fresh-seed path;
- one locked trait with enforced species slots, dominance, dropout, stabilization, and a real
  environmental or production effect;
- live soil multiplier, nutrients, moisture, light, temperature, biome, and elevation;
- analyzer/journal feedback explaining every contributing factor;
- one staged harvest and one meaningful resource tradeoff;
- deterministic unit tests for the calculations and one scripted in-game acceptance run.

Completion of that slice is the earliest point at which the project should claim that its new
genetics design works.

## Change log protocol

For every future feature commit, add an entry here containing:

| Date | Commit/PR | Stage | Claimed change | Verified behavior | Remaining gaps |
|---|---|---|---|---|---|
| 2026-09-18 | Working tree | 2 | Correct default vanilla crop/seed texture locations and keep generic `agri_seed` out of `SeedWrapper`. | `./gradlew build` succeeds; all 36 referenced default resources resolve; `runClient` builds the atlas with no `FML.TEXTURE_ERRORS` and joins the test world. | Full visual crop-stage and inventory checklist remains manual. |
| 2026-10-01 | Working tree | 2–6 | Canonical fresh-genome construction, deterministic inheritance RNG, built-in Winter Wheat hybrid, locked-trait slots/dropout/stabilization, and live light/temperature/frost effects. | 14 tests pass; dedicated server logs 11 plants and one resolved mutation, then reaches `Done`; in-client wheat planting serialized the expected fresh genome and saved cleanly. | Reload screenshot, end-to-end natural hybrid breeding, player-facing factor explanations, and balance QA remain. |
| _YYYY-MM-DD_ | _id_ | _stage_ | _what changed_ | _test or manual proof_ | _explicit follow-up_ |
