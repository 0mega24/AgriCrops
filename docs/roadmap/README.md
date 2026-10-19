# Agricrops long-term development plan

This directory records the fork's implementation history. Current vault design/milestones are
linked from [Decision 0002](../decisions/0002-java-in-game-scope.md): external simulation is
withdrawn, and Java/CT authoring, native imports/art reuse, staged genetics, layered soil and
in-game explanation drive the plan. This directory deliberately
separates three things that earlier commit messages and documentation sometimes blurred:

1. **Present in source** — a class, field, registry, or API exists.
2. **Mechanically connected** — normal gameplay can reach it and it affects the result.
3. **Complete** — it is usable, documented, balanced, tested, and available in a normal
   survival installation.

No feature should be called complete merely because its data model compiles.

## Documents

- [`00-master-tracker.md`](00-master-tracker.md) — authoritative meta list of prior work,
  current status, dependencies, decisions, and stage progress.
- [`../product-design.md`](../product-design.md) — repository-owned product definition, player
  journeys, glossary, principles, and acceptance mapping.
- [`../companion-simulator.md`](../companion-simulator.md) — buildable local web/CLI companion plan
  for pack validation, progression inspection, and deterministic farm simulation.
- [`01-product-definition.md`](stages/01-product-definition.md) — recover the intended product
  and make the design repository-owned.
- [`02-foundation-and-baseline.md`](stages/02-foundation-and-baseline.md) — stabilize the fork,
  build, identity, inherited systems, and playable baseline.
- [`03-genome-and-inheritance.md`](stages/03-genome-and-inheritance.md) — make the genome,
  species defaults, inheritance, and locked traits truthful and deterministic.
- [`04-soil-water-and-nutrients.md`](stages/04-soil-water-and-nutrients.md) — finish the soil
  simulation and resource economy.
- [`05-environment-and-adaptation.md`](stages/05-environment-and-adaptation.md) — complete biome,
  elevation, light, and temperature adaptation.
- [`06-breeding-and-progression.md`](stages/06-breeding-and-progression.md) — create an actual
  mutation graph and meaningful breeding progression.
- [`07-harvest-and-production.md`](stages/07-harvest-and-production.md) — complete staged harvests,
  yield, products, and resource tradeoffs.
- [`08-instability-and-weeds.md`](stages/08-instability-and-weeds.md) — turn weed mechanics into
  understandable, balanced gameplay.
- [`09-player-knowledge-and-tools.md`](stages/09-player-knowledge-and-tools.md) — analyzer,
  journal, discovery, scanner, feedback, and recipes.
- [`10-automation.md`](stages/10-automation.md) — implement real field control rather than
  telemetry-only switches.
- [`11-pack-authoring-and-content.md`](stages/11-pack-authoring-and-content.md) — make the
  CraftTweaker surface validated, explicit about startup/restart, and capable of complete content/imports.
- [`12-testing-migration-and-release.md`](stages/12-testing-migration-and-release.md) — tests,
  save compatibility, performance, packaging, and release gates.

## Status vocabulary

| Status | Meaning |
|---|---|
| Not started | No relevant implementation has been identified. |
| Source only | Types or APIs exist, but gameplay does not exercise them meaningfully. |
| Partial | A reachable path exists, but important behavior or usability is absent. |
| Functional | The main path works, but balance, UX, compatibility, or tests remain. |
| Verified | Acceptance criteria and automated/manual verification are complete. |
| Blocked | A named decision or external dependency prevents responsible progress. |

## Operating rule

Update the master tracker in the same commit as implementation work. A stage file owns detailed
scope and acceptance criteria; the master tracker owns cross-stage status. If implementation and
documentation disagree, mark the item partial and record the discrepancy instead of changing the
status optimistically.
