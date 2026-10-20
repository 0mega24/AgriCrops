# Building the new Agricrops

## Toolchain

Java 8, Gradle 8.9 (included wrapper), RetroFuturaGradle 1.4.0, Minecraft Forge 1.12.2 and
snapshot MCP mappings 20180710. The working build coordinates/wrapper are retained from the
archived prototype; gameplay sources and the rule module are new. No Node/npm or InfinityLib/
AgriCore dependency is required.

```sh
cd /home/slidy/rand/AgriCrops
./gradlew :core:test --no-daemon --max-workers=1
./gradlew build --no-daemon --max-workers=1
```

The first Forge build may need network downloads and Minecraft preparation. The pure core tests
do not launch Minecraft or require a display. Gradle defaults to one worker, no parallel projects
and a 512 MiB heap; tests use at most 128 MiB. Forge preparation may have additional tool processes
and is a separate qualification from core tests. Do not run `clean` reflexively: it removes
expensive prepared dependencies.

## Deliberate runtime checks

```sh
/home/slidy/rand/AgriCrops/gradlew --project-dir /home/slidy/rand/AgriCrops runClient
/home/slidy/rand/AgriCrops/gradlew --project-dir /home/slidy/rand/AgriCrops runServer
```

Run one Minecraft process at a time after builds finish. JavaExec tasks are configured with a
256 MiB initial / 1 GiB maximum heap. Use normal game exit or server `stop`; do not destroy
native windows to terminate the client. Compiling does not establish runtime or visual behavior.
Follow `docs/implementation-status.md`; headless crop checks are recorded separately.

The old dev world/configs remain in the local archive and are not copied into the new runtime.
The owner accepted Minecraft's EULA in this conversation. A new ignored development instance
was configured under `run/`, binding only `127.0.0.1:25575` with a new `first-slice-dev` flat world.
Its offline mode is for local testing only, not a public-server recommendation. Never open an old
Agricrops world with this new implementation as a migration test without an expendable copy.

On NixOS, `nix-shell` supplies Java/native libraries; the host also needs suitable graphics/audio.
A host kernel panic requires independent diagnosis; replacing the mod does not establish its cause.

## Artifacts and CI

The root mod jar includes the core classes and license/notice. The standalone core jar is for
Java development/testing, not another Forge mod. CI runs core tests followed by the mod build;
it does not run a GUI or accept EULAs.

Local archived generated Minecraft dependencies can be reused for bounded verification; record
that fact separately from clean-checkout CI qualification. Old mod classes/resources must never
be copied into new outputs. `docs/archive.md` identifies the preserved local material.

## CraftTweaker and acceptance fixture

CraftTweaker is compile-only and optional for players. Add `-PwithCraftTweaker` to a development
launch to include pinned CT `1.12-4.1.20.715`. Put scripts in `run/scripts/` and restart; see
`examples/scripts/first_crop.zs`. The ignored acceptance instance currently overrides wheat to
600 ticks per phase (2400 total); the built-in and checked-in example use 1200 (4800 total).

An opt-in operator fixture is available with `-PdevChecks`:

```sh
./gradlew --offline runServer --args=nogui -PdevChecks -PwithCraftTweaker -x compilePatchedMcJava -x processPatchedMcResources -x packagePatchedMc --no-daemon --max-workers=1 --console=plain
```

Use only a disposable world. The current fixture is `agricrops poc <x> <y> <z>`; legacy
`verify/saved` are retired. It first checks a bounded set of air positions, then creates eight
three-layer beds, lamps and a finite reservoir. Allow 15 blocks east and two blocks above the
chosen origin, plus three below. All positions must already be loaded. `agricrops pocSpawn`
chooses the existing spawn's X/Z at Y90 for a fresh world; it does not force-load farm chunks.
It leaves fixture blocks in place and intentionally accelerates normal paid intervals.

It checks recipes, native planting/harvest, actual parent stage windows, mixed native drops,
three stabilising generations, fresh recovery, clipping/rooting, real soil debits, tools, fluid
capabilities and persistence. With `examples/scripts/poc_ecology.zs` installed, it also checks
the gated native NBT import and custom factor. `agricrops assets` reports all presentation
fallbacks; `definitions` reports resolved timing. No command establishes client visuals.

After `save-all`, `stop` and restart with the same scripts/world, use
`agricrops pocSaved <original-x> <y> <original-z>` (or `pocSavedSpawn` for the original spawn
fixture). Evidence is one record per disposable world; a new successful fixture supersedes it.
Use `--args='nogui --world poc-base-v1'` without `-PwithCraftTweaker` for a separate base-only
qualification, never open the scripted import world with missing definitions accidentally.
The ignored working instance currently uses `poc-dev-v3`; old first-slice/archived worlds remain
untouched. All ordinary diagnostics still require operator permission; fixtures also require
the deliberate JVM development flag. Build/test never starts Minecraft automatically.

An intentionally failing script can make Minecraft refuse startup while Gradle still returns
success. Check for `Done`, definition errors and fixture markers, not just process exit status.
See `docs/poc-acceptance.md` and `docs/poc-gameplay.md` for exact current limits.
