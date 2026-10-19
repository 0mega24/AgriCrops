# New implementation status

Updated: 2026-10-19. Archive: `75d6f7baa52251ae7260156b4c23a0a4a9357dbb` on
`codex/archive-agricrops-fork-2026-10-19`.

## Present now

- Root Forge/RFG project and side-neutral mod entry point, new version `0.2.0-SNAPSHOT`.
- Independent Java 8 core, included inside the root mod jar.
- Immutable validated standard values and stable stat identifiers; explicit origin categories.
- Active-growth phase timeline separated from rendering, with positive durations/overflow checks.
- Bounded build/test defaults and Java CI workflow; no legacy gameplay/framework or web product.
- Crop sticks block/item/recipe and native wheat seed lifecycle: paid growth, light/soil checks,
  mature harvest/regrowth and native seed specimen drop/replant calculation.
- Water bucket and bone-meal inputs, simple nearby-water supply and position-owned surface pools.
- Strict versioned specimen/tile/soil persistence, retained rejected payloads and observed soil
  invalidation. Rejected saves do not silently become fresh genomes/fertility.
- Optional Java-backed CraftTweaker builders/explicit overrides, deferred registry resolution and
  freeze. Restart after script edits. Same Java definition model as built-ins.
- Borrowed vanilla stick/wood art and eight wheat models, localized basic inspection/tooltips.
  No new PNG required. The final wheat model is reserved for mature plants.
- Opt-in administrator acceptance fixture (`-PdevChecks`), not automatic game launch.

See `first-crop.md` for precise provisional mechanics and authoring limits.

## Verification

2026-10-19 local verification, Java 8:

- 26 tests pass: 17 pure core (6 genome, 5 timeline, 6 lifecycle/resource/catalog), 7 persistence
  and 2 static native-resource wiring tests. Zero failures/errors. Covers paid progress/yield,
  no repeated free harvest, blockers/depletion, overrides, strict decoding/quarantine and native
  stage/texture references. Static resources do not prove client model baking.
- `./gradlew --offline build --dry-run --no-daemon --max-workers=1` — task graph resolves.
- Bounded local Forge packaging succeeds with:

  ```sh
  ./gradlew --offline build -x compilePatchedMcJava -x processPatchedMcResources -x packagePatchedMc --no-daemon --max-workers=1
  ```

  This reuses only prepared Minecraft/Forge dependency source/classes/resources from the local
  archive. The new mod/core compile and package separately; archived Agricrops classes/resources
  are not included. Both dev and reobfuscated `agricrops-0.2.0-SNAPSHOT` jars are produced.
- A prior bounded packaging attempt without excluding `packagePatchedMc` hit the Fernflower
  download predicate in offline mode. Full clean dependency preparation/network build is still
  unverified; the cached-output build is not a clean-checkout qualification.
- CI is configured for core tests and a full Forge build but has not been run remotely.
- Three serialized headless servers reached ready, saved and exited normally via `stop`;
  see the runtime evidence below. No graphical client launched.

### Actual dedicated-server / CraftTweaker evidence

Effective JVM arguments were inspected before launch: server `-Xms256m -Xmx1G`, Gradle 512 MiB,
one worker. New disposable `run/first-slice-dev` world; loopback-only `127.0.0.1:25575`; no old
world opened. The owner explicitly accepted the EULA. See `BUILDING.md` for reproduction.

1. Without CT: 5 mods load; wheat resolves to 4800 ticks. Actual planting and accelerated paid
   interval growth/harvest pass: `FIRST_SLICE_OK ... progress=20 nutrients=180`.
2. With CT `1.12-4.1.20.715`: the actual `.zs` script compiles and queues wheat during CT INIT;
   `agricrops definitions` reports **2400 ticks**, the deliberate 600-per-phase override. Earlier
   crop and depleted soil restore: `SAVE_RELOAD_OK ... progress=200 nutrients=0 water=10000`.
   A fresh scripted field passes: `FIRST_SLICE_OK ... progress=20 nutrients=2580`.
3. Restart with the same CT script: scripted crop at `(4,90,0)` restores from disk:
   `SAVE_RELOAD_OK ... progress=320 nutrients=2280 water=9980` after normal loaded-world ticks.
   Expanded fixture at `(8,90,0)` confirms retained-seed genes/replant, no fertility reset,
   bucket replacement, fertilizer debit/no phase skip and regrowth:
   `FIRST_SLICE_OK ... progress=20 nutrients=4580 water=9980`.

Fixtures use real block interaction handlers, tile actions and NBT/WorldSavedData, but accelerate
intervals with a fake player. Drop calculation/direct block replacement is tested, not a real
player's entire breaking/pickup path. This is not a natural-time survival playthrough or visual
QA. Save/reload is actual normal shutdown and disk restart, not only helper serialization.
All servers are stopped; new runtime/logs/script/world stay ignored under `run/`.

The final end-of-world-tick soil-event deferral guard was rebuilt/tested after runtime checks;
canceled third-party block-event interactions still need a dedicated integration fixture.

Forge emitted dev-environment certificate/library-layout/translation/version notices; they did
not prevent ready/action/save/exit checks. Shared invalid-definition rules have unit coverage;
an actual intentionally failing CT-script boot is not qualified. Do not use archived reports as
evidence for this new implementation.

## Next gate / missing behavior

Milestone 1 has server evidence, **not full acceptance**. Next: client QA of crafting/placement,
all stages/icons, mature-only last stage, actual break/pickup/native seed replant, vanilla planting
coexistence, tooltips/inspection, resource reload and reconnect. No heavy client/server jobs
together; quit normally. The host kernel-panic cause remains undiagnosed.

Then staged breeding/regression/lockout/recovery, clippings and causal seed origins, real deeper
soil layers, researched field tools/journal, native adapters and bounded automation per vault
contracts. Only Growth/Yield affect production now; other stored stats/origin categories are
not completed mechanics. CT visuals are wheat-only, not generic/Pam/Mystical Agriculture imports.
Metadata/NBT matchers, full authoring and asset manifests are later work. Native-seed recipe
compatibility needs review before promising specimen safety across arbitrary other mods.
