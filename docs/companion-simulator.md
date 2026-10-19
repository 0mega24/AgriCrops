# Agricrops companion simulator plan

> **Withdrawn by owner direction, 2026-10-19.** The remainder is an archived proposal, not an
> active backlog or release gate. Agricrops is planned as an all-Java mod with CraftTweaker
> content/imports and in-game tools/research. Java tests remain verification tooling. See
> `decisions/0002-java-in-game-scope.md` and the linked vault contracts. Existing prototype source
> is retained without requiring it to be finished or built.

## Historical proposal

## Purpose

Build a local-first, browser-based tool that accepts Agricrops configuration and supported
ZenScript definitions, validates them, visualizes progression, and simulates a farm over time. It
serves pack authors first and can later provide a discovery-respecting planning mode for players.

The simulator must be buildable from this repository and testable without Minecraft. Its results
become authoritative only for mechanics backed by shared versioned fixtures against the Java mod.

## User workflows

1. Select a pack directory or drop in `config/agricrops/config.cfg` and `scripts/*.zs` files.
2. See parse errors and semantic diagnostics with file/line locations.
3. Inspect the resolved soils, species, plants, attributes, loot tiers, and mutation graph.
4. Lay out a small grid farm, choose soil/environment/seed genomes, and advance ticks or days.
5. Compare repeated seeded runs for growth, resource consumption, offspring distribution, harvest,
   instability, and weed spread.
6. Export a validation report and a normalized resolved-model JSON file suitable for CI.

## Architecture

### Shared model boundary

Introduce a versioned `agricrops-model-v1` document containing only resolved data: configuration
values, soil profiles, plants, species, mutations, attributes, and formula parameters. Minecraft
registry objects and CraftTweaker objects never cross this boundary.

The Java mod owns definition resolution and eventually exposes a Gradle/CLI task such as
`exportResolvedModel`. During bootstrap, the companion may parse the documented Agricrops
ZenScript subset, but conformance fixtures must prove it resolves the same data as Java. Unknown
ZenScript control flow, imports, or calls produce an explicit unsupported diagnostic rather than
being executed.

### Simulator engine

Use a pure deterministic engine with an injected seeded PRNG and no DOM dependencies. Model a
bounded 2-D crop grid, crop/soil state, tick scheduling, growth, resource drain/refill, crosses,
inheritance, harvest tiers, instability, cure actions, and weed spread. Each event is appended to a
trace so a user can explain why a result occurred.

Simulation modes:

- **Exact fixture mode:** mechanics already specified and parity-tested against Java.
- **Experimental mode:** proposed balance formulas, visibly labelled and never reported as current
  in-game behavior.
- **Monte Carlo mode:** repeated seeded runs with distributions and confidence summaries.

### Web application

A TypeScript workspace is the preferred implementation because it can provide a static browser UI
and a Node-based CI command from the same parser/model/engine packages. Start with a framework-light
Vite application; keep parser, validator, and engine packages independent from the UI so another
frontend or JVM implementation can replace it.

Suggested workspace:

```text
companion/
  package.json
  apps/web/                 farm grid, inspectors, charts, diagnostics
  packages/model/           generated types and schema validation
  packages/zenscript/       safe parser for supported mods.agricrops calls
  packages/engine/          deterministic simulation and event traces
  packages/cli/             validate/simulate/export for CI
  fixtures/                 shared inputs and expected normalized outputs
```

No remote service is needed for the first release. File contents remain in the browser unless the
user explicitly runs the local CLI. A future live-world bridge requires a separate security design
and is not part of this plan.

## Web UI outline

- **Pack workspace:** files, parse status, schema/API version, unresolved references.
- **Definition explorer:** searchable plant/species/soil/attribute tables and detail panels.
- **Mutation graph:** parent pairs to child species, unreachable nodes, cycles, and discovery masks.
- **Farm canvas:** paint soil/crop sticks/seeds; edit environment and resources; run/pause/step.
- **Inspector:** genome, phenotype, growth factors, resource pools, risks, and event explanation.
- **Results:** time-series charts, harvest totals, offspring/stat distributions, failures, and a
  downloadable report.

## Delivery sequence

1. **Freeze contracts:** specify model v1, configuration keys, units, ranges, RNG semantics, and
   shared golden fixtures. Extract pure Java calculations where necessary.
2. **Validator CLI:** build the TypeScript workspace, parse the Agricrops ZenScript subset and
   config, normalize definitions, validate references/ranges/progression, and emit JSON/report.
3. **Read-only web explorer:** load local files, show diagnostics and resolved definitions, render
   the mutation/progression graph.
4. **Deterministic single-plot engine:** growth, environment, water/nutrients, staged harvest, and
   event trace with Java parity tests.
5. **Farm simulation:** grid interactions, crosses/inheritance, instability/weeds, actions,
   Monte Carlo batches, charts, save/load scenarios.
6. **Repository integration:** Gradle wrapper tasks (`companionBuild`, `companionTest`, and
   `validatePack`), CI artifacts, bundled docs, and a release/version compatibility matrix.
7. **Optional future bridge:** import a server-exported snapshot or connect read-only to a dev
   server only after authentication, protocol versioning, and thread-safety are designed.

## Implemented foundation (2026-10-01)

`companion/` now contains a pinned TypeScript/Vite workspace with a responsive local web UI, a
versioned resolved-model type and validator, a conservative parser for declarative mutation calls,
a seeded single-plot engine, event traces, tests, and a production build. Gradle exposes
`companionTest` and `companionBuild`. This establishes delivery steps 1–3 only in narrow form; it
does not yet satisfy the v1 definition of done below.

## Build and test contract

The completed companion must support reproducible commands equivalent to:

```sh
npm ci --prefix companion
npm test --prefix companion
npm run build --prefix companion
npm run validate --prefix companion -- --config run/config/agricrops/config.cfg --scripts run/scripts
```

Gradle wrapper tasks will invoke these commands when Node is available, while the normal mod build
must remain usable without Node until the companion is packaged as a release requirement.

Required tests include parser locations/errors, schema migration, graph validation, deterministic
snapshots, statistical sanity, Java/TypeScript golden parity, malformed-file fuzz cases, and a
Playwright smoke test covering file load, farm step, and report export.

## Definition of done for v1

- A clean clone can build and test the CLI and static web app using documented pinned tooling.
- All supplied ZenScript examples and generated Agricrops config parse or give honest unsupported
  diagnostics.
- Invalid references, ranges, duplicate IDs, unreachable progression, and unsafe reload assumptions
  are reported with source locations.
- A user can build a farm, seed the RNG, simulate a fixed duration, inspect every event, and export
  a reproducible scenario/report.
- Golden scenarios match the Java rules for every mechanic labelled exact.
- The app works locally without uploading pack files or requiring a hosted backend.

## Dependencies and risks

- Current Java rules are not yet complete or consistently deterministic; Stage 3 and the vertical
  slice must stabilize them before full parity is possible.
- ZenScript is a programming language. Supporting arbitrary scripts would require executing
  untrusted code or embedding its runtime, so v1 intentionally accepts only the declarative
  `mods.agricrops.*` call subset.
- Current `/ct reload` behavior is not transactional; the simulator must diagnose this rather than
  imply that a validated edit is live in Minecraft.
- Third-party item existence and mod-specific behavior require a supplied registry snapshot or
  explicit stubs; they cannot be inferred reliably outside Minecraft.
