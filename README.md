# Agricrops

A new Java implementation of the crop genetics and field-management project for Minecraft
Forge 1.12.2. This branch starts the replacement; it is not yet a playable farming release.

## Current foundation

- A minimal side-neutral Forge entry point and mod metadata.
- An independent Java 8 `core` module with immutable standard genetic values, explicit seed
  origin categories and active-time biological phases independent from texture stages.
- Unit tests for adaptation bounds, invalid templates, immutability and phase timing.
- One-worker build defaults and no external simulator, Node/npm, AgriCore or InfinityLib dependency.

Crop blocks, persistence, inheritance/locked traits, resources/layers, CraftTweaker registration,
imports, field tools and journal are still to be implemented. Existing concepts are requirements,
not claims about this foundation.

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
