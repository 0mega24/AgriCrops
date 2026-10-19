# Dedicated-server smoke check — 2026-09-16

Environment: Linux, Azul OpenJDK 8 (`1.8.0_492`), Gradle 8.9, Forge 14.23.5.2847,
Minecraft 1.12.2.

## Commands

```sh
./gradlew build
./gradlew runServer
```

The first server run was configured for local testing with `online-mode=false`; the person running
the check explicitly accepted the Minecraft EULA at RetroFuturaGradle's prompt.

## Result

- `build` completed successfully; Gradle reported `test NO-SOURCE`.
- Forge identified and loaded six mods, including `agricrops` and `infinitylib`.
- Agricrops registered the built-in soil profiles, ten fork-native plants, and demo species rules.
- The server created the development world and reached `Done (5.521s)! For help`.
- The `stop` command saved the world, unloaded dimension 0, and ended with `BUILD SUCCESSFUL`.

This proves headless loading and clean shutdown. It does not replace the crop-lifecycle fixture or
visual client checklist required by Stages 2 and 12.
