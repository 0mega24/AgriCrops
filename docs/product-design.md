# Agricrops product design

Status: repository design baseline, awaiting owner acceptance  
Design version: 0.1  
Last updated: 2026-10-19

> Owner scope amendment: the external simulator is withdrawn. Current detailed design lives in
> the Agricrops vault specification and breeding/soil, imports/assets, information-design and
> delivery contracts linked from [Decision 0002](decisions/0002-java-in-game-scope.md).
> Required direction includes Java/CT content, native art/crop imports, regression/lockout,
> phase timing, clippings/seed origins, layered soil and in-game self-documentation. Concrete
> proposed defaults remain under review; this planning revision does not implement them.

## Product decision

Agricrops is a **standalone crop-breeding and field-management mod with an optional pack-authoring
layer**. The distributed jar must ultimately contain a small, coherent progression that works with
Minecraft alone. CraftTweaker/ZenScript may extend or replace the built-in content for packs, and
OpenComputers may add telemetry and automation, but neither is required for the core player loop.

The player fantasy is running a living experimental farm. The player observes conditions, breeds
plants toward a purpose, accepts tradeoffs rather than maximizing every number, discovers hybrids
and traits, and eventually manages larger fields with tools and bounded automation.

## Design principles

1. **Readable causes.** A failed growth tick, mutation, lost trait, weed conversion, or poor harvest
   must have a player-visible explanation.
2. **Useful tradeoffs.** Fast or highly mutable crops must create costs or risks; negative bipolar
   values describe adaptations, not inferior crops.
3. **Progressive discovery.** The rules are deterministic enough to plan, while exact outcomes may
   be probabilistic and become clearer through observation and research tools.
4. **One authoritative model.** Java runtime rules, validation, in-game documentation and tests
   use one versioned definition model. There is no separate simulator application.
5. **Honest extensibility.** Invalid or incomplete packs fail with actionable diagnostics. A source
   type or registry entry is not a completed feature until it is reachable and understandable.

## Player journeys

### First 30 minutes

The player crafts crop sticks and a basic inspection tool, converts familiar seeds into an
Agricrops planting, and learns that soil moisture and nutrients affect growth. Inspecting a crop
shows its species, growth stage, broad condition, and unanalysed genetic potential. The base game
supplies at least two immediately plantable crops and makes the next attainable hybrid visible as a
goal without revealing every outcome.

### First five hours

The player builds a small breeding plot, uses an analyzer and journal to compare genomes, and
crosses three parent crops into a first hybrid. They deliberately trade growth speed against water,
nutrient, or instability pressure; recover at least one destabilizing crop; and use staged harvests
for a meaningful choice between harvesting early or waiting. The journal records discovered
species, mutations, and environmental observations.

### Late game

The player operates multiple purpose-built lines: robust field crops, high-yield resource crops,
and specialist crops adapted to unusual environments. Sensors and bounded actuators reduce manual
work but still consume resources and obey the same rules. Pack-provided content can extend the
mutation graph through CraftTweaker. In-game validation/author tools explain resolved content;
the journal and field tools teach permitted discoveries and actionable field requirements.

## Core loop

1. Prepare a soil profile and supply water/nutrients.
2. Plant or cross crops whose genome and species fit the intended environment.
3. Observe growth, resource use, stress, instability, and phenotype.
4. Harvest now for an available tier or wait for a later reward.
5. Analyze offspring, keep promising seeds, and record discoveries.
6. Repeat toward a hybrid, adaptation, trait, or production goal.

## Genetics promise

Every seed has a species and a genome. The eight standard stats have fixed ranges and documented
semantics. Magnitude stats represent more or less of an effect; bipolar stats represent different
adaptations around a generalist midpoint. Crosses combine parents using deterministic, seeded
randomness and explicit per-stat policies. Locked attributes have species slots, dominance,
carrier/expression rules, dropout, stabilization, and a real effect before they are exposed as
finished content.

Genetics is **predictable at the rule level, probabilistic at the offspring level, and
progressively discoverable in the UI**. A fixed world seed and fixed inputs must permit repeatable
tests even when a player cannot predict a single roll.

## Built-in content promise

The first complete vertical slice contains three parent crops and one discoverable hybrid, one
locked trait with a gameplay effect, a small but live environmental/resource model, one staged
harvest choice, and journal/analyzer explanations. Additional species are added only after this
slice is verified. Existing texture-driven placeholder registrations do not satisfy this promise.

## Optional integrations

- **CraftTweaker:** supported pack-authoring surface. Definitions need transactional validation and
  a documented reload lifecycle before reload is advertised as reliable.
- **OpenComputers:** optional telemetry and bounded actuation. The base game retains non-OC ways to
  inspect and maintain a field.
- **JEI and probe mods:** convenience and explanation surfaces, never exclusive access to required
  progression information.
- **Crop imports:** Java adapters/CT mappings borrow native art and preserve source identity,
  outputs and applicable progression gates, with explicit unsupported behavior and asset reports.
- **In-game author tools:** Java validation, stage previews, provenance and localization/asset
  diagnostics replace the external workbench.

## Compatibility policy

Minecraft 1.12.2 and Java 8 are the target. Compatibility with upstream AgriCraft worlds, the
fork's earlier 0.1.0 saves, legacy JSON definitions, and upstream automation blocks is not promised
until a versioned migration matrix exists. Unsupported data must be rejected or backed up with a
clear message; it must not be silently reinterpreted.

## Non-goals

- Supporting Minecraft versions other than 1.12.2 during the vertical-slice milestone.
- Reproducing all upstream AgriPlants content or every upstream integration.
- Making OpenComputers or CraftTweaker mandatory for normal play.
- Shipping an external simulator/browser service/JavaScript product.
- Assuming generic imports automatically reproduce arbitrary source-mod mechanics.
- Treating a maximized genome as the only optimal endpoint.
- Maintaining two independently implemented sets of formulas.

## Glossary

| Term | Meaning |
|---|---|
| Plant | Registered content definition: growth stages, textures, products, habitat, and weed flags. |
| Species | Genetic template associated with a plant, including fresh-seed defaults and allowed locked-trait slots. |
| Genome | A seed's heritable standard stats and locked-attribute alleles. |
| Stat | One of the eight fixed signed standard genetic values. |
| Allele | One inherited copy of a locked attribute, including value and dominance information. |
| Locked attribute | Open-ended named genetic trait outside the eight standard stats; it must have inheritance and a gameplay effect. |
| Mutation | Registered parent combination capable of producing a different child species. |
| Spread | Same-species propagation into adjacent cross-crop sticks, potentially producing changed stats. |
| Soil profile | A block's soil composition, nutrient capacity, moisture retention, and growth multiplier. |
| Soil column | Persistent field state associated with soil, including current moisture and nutrients. |
| Harvest tier | The best eligible drop definition for the crop's present growth stage. |
| Discovery | Player knowledge recorded after observing/analyzing species, mutations, or traits. |
| Scanner | A field-level sensor that reports crops and conditions over a bounded area. |
| Actuator | A bounded, resource-costed mechanism that changes field conditions. |

## Promise-to-roadmap acceptance map

| Design promise | Roadmap owner | Minimum acceptance proof |
|---|---|---|
| Playable base jar | Stages 2 and 6 | New survival world reaches the first hybrid without optional mods. |
| Truthful genomes | Stage 3 | Fixed-seed creation/inheritance tests cover every value and allele state. |
| Live field resources | Stages 4 and 5 | In-game fixture demonstrates soil, water, nutrients, light, temperature, biome, and elevation effects. |
| Meaningful harvest/risk | Stages 7 and 8 | Fixture proves staged reward choice, instability warning, cure, and recovery. |
| Understandable mechanics | Stage 9 | Player completes vertical slice using in-game feedback, without reading source. |
| Bounded automation | Stage 10 | Automated field consumes resources and cannot bypass genetics/environment rules. |
| Safe pack extension | Stage 11 | Invalid graph fails in CI; valid graph reloads or explicitly requires restart. |
| Custom content/imports | Stages 11 and 12 | CT expresses supported rules; pinned native imports retain identity/gates and tested borrowed presentation with an accurate asset report. |
| Information depth | Stage 9 | Localized tools/journal explain permitted knowledge; internal NBT is not dumped into ordinary UI. |
| Trustworthy release | Stage 12 | Build, tests, dedicated-server lifecycle, client checklist, and migration gates pass. |

## Acceptance and change control

This document replaces missing references to `crop-mod-design.md` as the repository design
baseline. Changes that alter product mode, stat meaning, compatibility, or required dependencies
require a decision record. Acceptance of this baseline by the project owner completes the only
remaining Stage 1 gate.
