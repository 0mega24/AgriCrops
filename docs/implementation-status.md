# New implementation status

## Current checkpoint — 2026-10-20 small-content POC

The first-crop-only checkpoint below is now **historical**. This POC extends `933a60aa` on main;
the archive and baseline history are unchanged. Publication refs are recorded in Git and the
Agricrops vault status note. Current guide:
`poc-gameplay.md`. Detailed evidence and remaining qualification: `poc-acceptance.md`.

- Three vanilla-backed parent crops; eight consumed genes, source-labelled environmental
  responses, real three-cell root zones, distinct role phases and paid propagation.
- Bounded mixed existing-parent products, persisted allocation/funding, numeric regrowth,
  regression, competing/fixed trait paths and fresh-line backcross recovery.
- Native/crafted/bred/clipping origins, paid seedling clipping/rooting and retained generic
  carriers that do not transfer prepaid production or soil inventories.
- Manual care, compost release, bounded passive/rain intake and finite supplied field reservoirs
  with water-only Forge fluid capability, fair round-robin targets and persisted source stocks.
- Probe, paper-paid analyser, player discoveries and localized chat field journal; ordinary
  tooltips and crop update packets do not print/sync the hidden genome.
- Shared Java/actual CT crop/environment/soil/trait definitions, metadata/NBT native imports,
  explicit hybrid permissions and known native fallback profiles. `/agricrops assets` reports
  required presentation: zero mandatory new PNGs for this POC.
- Strict schema-2 specimen/soil migration and quarantine, including rejected reservoir retention.

Latest complete suite: **61 tests** (43 core, 14 persistence, 4 static resource wiring), zero
failures/errors. Local dev/reobfuscated artifacts build using the same bounded cached Forge
dependency exclusions documented below; clean preparation/remote CI remain separate.

Real base-only (five mods) and CT (six mods, pinned 1.12-4.1.20.715) disposable-world action and
disk-restart checks pass. The fixture uses real recipes, handlers, source writes, adjacent plots,
three stabilising generations, paid recovery, native outputs, clipping/rooting and finite fluid
storage. CT also qualifies a tagged native item import and an authored factor. A deliberately
invalid requirement refuses startup with the source file/line before opening a world; the
temporary failing script was removed and a valid ready/action run restored. See acceptance log.

This is a **small functional POC**, not a finished farming release or performance qualification.
No graphical client was launched. Manual visuals/full player break-pickup/reconnect, readable
tool UI/pagination, survival balance, cancelled external events and long-run large farms remain
open. Actual Pam/MA versions/catalogues/special mechanics, real pollution/season addons, hopper
item supply and broad automation/content are not implied by generic extension points.
All game processes are deliberately serialized, capped at 1 GiB and normally stopped; no claim
is made that the earlier host kernel-panic cause is diagnosed.

## Historical first-crop checkpoint — 2026-10-19

The following records describe the prior slice only, including its then-missing behavior and
old resource defaults. Use the POC guide/acceptance checklist for current behavior.

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
- Numeric right-click regrowth: default stored stage 1, CT `.regrowFrom(0..6)` for custom earlier/
  intermediate stages. Stage 7 is mature and rejected; genome/origin remain unchanged.
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

- 34 tests pass: 24 pure core (6 genome, 5 timeline, 6 lifecycle/resource/catalog, 7 numeric regrowth), 8 persistence
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
- Initial three headless servers and a numeric-regrowth follow-up reached ready, saved and
  exited normally via `stop`; see the evidence below. No graphical client launched.

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

Numeric harvest follow-up: actual CT `.regrowFrom(4)` resolves as `regrowthStage=4@1372` with
the 2400-tick test timeline. At `(16,90,0)`, the real harvest handler passes assertions for stored
age 4, preserved genome/origin, no repeat output and post-harvest NBT. Full fixture reports
`FIRST_SLICE_OK ... harvestRegrowth=1372`, then saves/stops normally. This verifies server state,
not client appearance. The temporary test override is restored to default `.regrowFrom(1)`
afterward; the baseline source example explicitly uses 1. No NBT schema change is needed because
the existing active progress round-trips the reset point.

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
