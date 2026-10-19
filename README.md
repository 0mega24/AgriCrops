# Agricrops

A new Java implementation of the crop genetics and field-management project for Minecraft
Forge 1.12.2. An early wheat lifecycle now runs; this is not a finished farming release.

## Current first slice

- Craftable crop sticks that accept vanilla wheat seeds, borrow native wheat models and retain
  the specimen through harvest and recovered seeds.
- Resource-paid active growth, light/soil checks, watering, fertilizer and localized inspection.
- Position-owned saved water/nutrients, strict versioned specimen NBT and rejected-data retention.
- Optional Java-backed CraftTweaker builders, explicit overrides and startup validation; a real
  script override has been tested on a dedicated server.
- An independent Java 8 `core` module with immutable standard genetic values, explicit seed
  origin categories and active-time biological phases independent from texture stages.
- Tests for bounds, timing, resource conservation, persistence and native asset wiring.
- One-worker build defaults and no external simulator, Node/npm, AgriCore or InfinityLib dependency.

Inheritance/locked traits, breeding/clippings, deeper soil layers, full crop imports, field tools
and journal are still to be implemented. Only wheat presentation is qualified by static checks;
client visual QA remains open. See [the first-crop guide](docs/first-crop.md).

## Build

Use Java 8 and the included Gradle 8.9 wrapper:

```sh
./gradlew :core:test
./gradlew build
```

Client/server launch tasks exist through RetroFuturaGradle, but are deliberate manual checks,
never part of test/build. See [BUILDING.md](BUILDING.md).

## Design and next work

The Obsidian project folder `/home/slidy/Vault/Projects/Agricrops/` is the product source of truth.
[docs/design.md](docs/design.md) maps its mechanics, breeding/soil, imports/assets and information
contracts into the implementation. [docs/implementation-status.md](docs/implementation-status.md)
records actual progress and verification.

The product is Java with optional CraftTweaker custom content/imports and in-game learning tools.
Vanilla/native mod seed and crop art should be borrowed where suitable. Genetics includes
tradeoffs, regression and recoverable seed-specific closed paths; development and reproduction
windows are not tied to a fixed number of textures.

## Previous fork and recovery

The complete old source, docs, tests and withdrawn companion are on
`codex/archive-agricrops-fork-2026-10-19`, commit `75d6f7baa52251ae7260156b4c23a0a4a9357dbb`.
No Git history was rewritten. [docs/archive.md](docs/archive.md) explains recovery and locally
preserved worlds/build results. New code does not promise compatibility with old saves.

## License

MIT. See [LICENSE](LICENSE) and [NOTICE](NOTICE) for retained attribution and project history.
