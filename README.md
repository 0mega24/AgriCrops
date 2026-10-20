# Agricrops

A new Java implementation of the crop genetics and field-management project for Minecraft
Forge 1.12.2. A small-content farming/genetics POC now runs; this is not a balanced release.

## Current POC

- Wheat, beetroot and carrot; native art, numeric harvest reset and retained specimens.
- Environmental response, three position-owned soil layers, compost and finite supplied irrigation.
- Paid stage-window breeding, bounded mixed-parent drops, regression, competing/fixed traits,
  fresh-line recovery, seedling clippings/rooting and distinct acquisition origins.
- In-game probe, paid analyser and discovery-aware field journal (chat presentation for the POC).
- Versioned genes/composition/traits/ancestry and production funding, with rejected-data retention.
- Optional Java-backed CraftTweaker builders, explicit overrides and startup validation; a real
  script override has been tested on a dedicated server.
- An independent Java 8 `core` module with immutable standard genetic values, explicit seed
  origin categories and active-time biological phases independent from texture stages.
- Explicit native item metadata/NBT imports, fallback visual profiles and shared CT ecological rules.
- Tests for bounds, timing, conservation, inheritance/recovery, persistence and native asset wiring.
- One-worker build defaults and no external simulator, Node/npm, AgriCore or InfinityLib dependency.

See [POC gameplay/configuration](docs/poc-gameplay.md) and [acceptance evidence](docs/poc-acceptance.md).
Client visual/manual QA, polished tool UI, survival balance, broad defaults and actual Pam/Mystical
Agriculture version qualification remain separate work; generic import support is not an automatic
catalogue or imitation of every source mod's mechanics.

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
