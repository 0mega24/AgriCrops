# New implementation status

Started: 2026-10-19. Archive: `75d6f7baa52251ae7260156b4c23a0a4a9357dbb` on
`codex/archive-agricrops-fork-2026-10-19`.

## Present in this foundation

- Root Forge/RFG project and side-neutral mod entry point, new version `0.2.0-SNAPSHOT`.
- Independent Java 8 core, included in the eventual root mod jar.
- Immutable validated standard values and stable stat identifiers; explicit origin categories.
- Active-growth phase timeline separated from rendering, with positive durations/overflow checks.
- Bounded build/test defaults and Java CI workflow; no legacy gameplay/framework or web product.

## Verification

2026-10-19 local verification, Java 8:

- `./gradlew --offline :core:test --no-daemon --max-workers=1` — succeeds; 11 tests
  (6 standard-genome, 5 growth-timeline), zero failures/errors.
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
- No Minecraft process/client/server was launched. Runtime and survival gameplay remain unverified.

Do not use old archive reports as proof of new gameplay or call this foundation a playable mod.

## Next milestone / missing behavior

The foundation is not playable crop gameplay. Still needed: plant/content resolution and CT spike,
one crop block/seed with borrowed vanilla art, resource-affordable progress/actions, root-zone
persistence/invalidation, localized in-game inspection and actual save/reload. Then implement
inheritance/loci/regression, staged breeding/clippings, layered content, journal/tools, native
imports and bounded automation according to the vault's acceptance plan.

No Minecraft client/server was launched during archiving. Host kernel-cause investigation and
deliberate new-runtime qualification remain separate from building this scaffold.
