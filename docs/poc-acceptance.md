# Feature-complete small POC acceptance

Work started 2026-10-20 against `933a60aa`. This is an implementation checklist, not a completion
claim based on the checklist alone. Product contract: vault `Plant physiology and dynamic hybrids.md` plus breeding/import/
information contracts. All gameplay is Java/Forge 1.12.2 with optional real CraftTweaker authoring.

| Path | Small default / required proof | Status |
|---|---|---|
| Environmental response | Biome/dimension/Y, temperature, humidity proxy, authored air quality, light/rain/exposure; explain source and effects without rewriting genes | Core + actual position/temperature/rain/probe assertions pass; optional live pollution/season provider not qualified |
| Roots/resources | Three actual position-owned cells, reach, transfer/evaporation/drainage, compost reserve release, no replacement refill | Core conservation + world third-layer debit/seedling exclusion/compost/replant assertions pass |
| Supply | Manual water/fertilizer/compost, actual rain and bounded passive water; supplied reservoir with real debits and fair bounded targets | Manual inputs, finite controller, Forge fluid input/debit and persistence pass; rain/long-farm throughput needs natural-time exercise |
| Genes/traits | All eight values have consumers; small named trait set, carrier/expression/stability, competing fixed locus and paid recovery | Core expression/regression/conflict tests + three paid world generations and fresh recovery pass |
| Breeding | One eligible canonical parent pair/role windows; same-species and dynamic compatible hybrids; regression and bounded component inheritance | Core + real adjacent-plot hybrids/carrier generations pass, including distinct default role phases |
| Production | Native mixed drops, one paid cycle budget, persisted allocation, numeric regrowth; no repeat/late-stage free output | Core + actual native mixed harvest/drop/replant + disk restart pass |
| Origins/propagation | Native foundation, deterministic crafted seed, bred seed, paid juvenile clipping and rooting; no cloned funds/generations | Actual planting/crafted origin/shears/clipping replant/rooting assertions pass |
| In-game learning | Probe/analyzer/notebook and localized actionable reasons; hidden genotype not dumped in normal tooltip | Actual handlers/paper debit/player discovery/filtered packet assertions pass; client reading/tooltip QA open |
| Authoring | Defaults and CT share models; validated environmental/soil/crop/trait policies, real script lifecycle and errors | Actual CT 1.12-4.1.20.715 scripts + gated NBT item input/output pass; invalid requirement rejects startup with file/line |
| State/visuals | Versioned bounded specimen/soil/reservoir state, explicit v1 migration/quarantine, native wheat/beet/carrot presentations | Persistence/migration/quarantine + static resource wiring pass; model baking/visual QA open |
| Verification | Pure boundary tests, Forge action scenarios, bounded dedicated server ready/save/restart/normal stop, artifact packaging | Cached-dependency build/tests and serialized headless action/restart runs pass; clean checkout/remote CI/client gates open |

Default content stays small: three parent crops, a few trait alleles and soil profiles, four active
product components maximum. Chat/book-style tools are acceptable POC presentation, not finished
GUI design. External pollution/season adapters, Pam/MA versions, broad automation/content and
client visual QA are separately qualified, not claimed because extension points exist.

One game process at a time, server heap capped at 1 GiB, Gradle one worker/512 MiB. No automatic
graphical launch following the reported host panic. Never use an old production save directly.

## Recorded headless evidence — 2026-10-20

Final complete build: 61 tests, zero failures/errors (43 core, 14 persistence, 4 static resource
wiring). Dev and reobfuscated jars produced. Current distribution:
`build/libs/agricrops-0.2.0-SNAPSHOT.jar`, SHA-256
`b9a47a59a651de6131913dfc4edb018c4d73839d28621f6925da7c546909aebc`.
Publication refs are recorded in Git and the Agricrops vault status note. Local runtime worlds,
logs, build outputs and recovery files remain excluded from source commits.

Java 8; server `-Xms256m -Xmx1G`, Gradle 512 MiB, one worker. Loopback `127.0.0.1:25575`.
No GUI, no old production world, no concurrent game processes. Test fixture deliberately advances
world-total time and invokes normal crop intervals; it supplies labelled finite test inputs.
It does not wait out natural growth, simulate chunk unloading, or claim a real player's entire
break/pickup/client experience. It does execute actual planting/harvest/shears/tool handlers,
root source writes, paid plot breeding, recipes, fluid capability and WorldSavedData.

- CT world `run/poc-dev-v3`, wheat script 600 ticks/phase; three Java default parents plus the
  explicit qualification-only tagged melon. At `(0,90,4)`, `POC_WORLD_OK` at 16:18:37 EDT.
  Actual analyser consumes paper and records the introduced trait; native seed NBT gate and
  harvested product NBT are asserted. Saved and stopped normally.
- Actual disk restart: `POC_SAVE_RELOAD_OK` at `(0,90,4)`, 16:20:13 EDT. Identity, products,
  alleles, origin, lineage/generation, production price/funding and finite tank retained.
- No CT: five mods loaded in separate `run/poc-base-v1`. Built-in durations 4800/4000/5200.
  `POC_WORLD_OK` at `(641,90,689)`, 16:25:08. Save/restart passes at 16:27:37, followed by an
  updated full fixture at `(641,90,697)` with different default breeding roles, real crafting
  and water-only Forge fluid capability checks. Normal save/stop.
- Deliberate invalid requirement script: startup fails before opening `poc-invalid-v1` at
  16:28:08 with `poc_invalid.zs:2 / poc:invalid: Invalid environmental band for agricrops:temperature`.
  Temporary script removed. Forge writes an expected application crash report; this is not a
  host/kernel panic. Gradle reports task success despite Minecraft refusing startup, so the
  qualification checks the log/state, not only Gradle's exit status.
- Initial fixture-only `WorldServer.setTotalWorldTime` mapping mismatch was found and corrected
  to the actual `WorldInfo.setWorldTotalTime` API. Server stopped normally, then the full action
  suite passed. Failed disposable fixture blocks are retained, not confused with accepted runs.

## Remaining qualification, not hidden completion claims

Final updated CT run: previous `(0,90,8)` hybrid restores at 16:33:51 EDT; the complete fixture
at `(0,90,12)` passes at 16:33:53, including full smaller-capacity soil refusing to consume care
items, registered-world rejected-reservoir NBT retention and pipe isolation. Normal save/stop.
The valid CT scripts remain installed; the deliberate invalid script is absent.

Final dispatch qualification: at 16:42:10 EDT, `(0,90,12)` restores and the full `(0,90,16)`
fixture passes through `PlayerInteractionManager.processRightClickBlock`, not direct block-handler
calls. This includes second-stick consumption and actual sneak-bone-meal recovery toggling. A
targeted Forge interaction hook allows those explicit controls without overriding cancelled or
denied interactions. Active-product hard habitat gates have a dedicated no-debit regression
test; the probe reports source/factor blockers. Normal save/stop at 16:42:16.

Manual client rendering/icons/tooltips, actual breaking/pickup/replant/reconnect, creative slot
behavior, survival pacing, cancelled third-party events, natural rain/drying and long-run many-bed
throughput still need exercise. The host panic's cause is not established. Generic imports are
implemented; actual Pam/MA versions, automatic catalogues, special source-mod mechanics, hopper
item supply and a polished paginated research GUI are not qualified/implemented by these tests.
Live optional provider interfaces do not mean a real pollution/season addon is present.

After those checks, expand defaults/actions one at a time with regression fixtures. Do not add
dozens of crops before the small loop has manual player sign-off.
