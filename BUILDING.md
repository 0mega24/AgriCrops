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
native windows to terminate the client. Runtime startup and visual behavior are not established
by compiling this scaffold. Follow the verification record in `docs/implementation-status.md`.

The old dev world's EULA acceptance/configs remain in the local archive and are not copied into
the new runtime. The owner accepted Minecraft's EULA in this conversation; complete the actual
development server setup deliberately when runtime testing is resumed. Never open an old
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
