# Building Agricrops

Agricrops is a Forge **1.12.2** mod, hard-forked from AgriCraft. It builds with
**RetroFuturaGradle (RFG)** — the maintained replacement for the long-dead
ForgeGradle 2.x — on **Gradle 8.9**, targeting **Java 8**. This is the same core
build stack GregTech CEu and the GTNH ExampleMod use.

## Prerequisites

- **Java 8** (Forge 1.12.2 targets it; Gradle 8.9 runs fine on JDK 8).
- **Git**.
- On **NixOS**: use [`shell.nix`](./shell.nix) (`nix-shell`) — `runClient` needs
  prebuilt LWJGL/GLFW natives in an FHS layout. See `crop-mod-getting-started.md` §1.

## Build

```sh
nix-shell            # NixOS only; otherwise just make sure Java 8 is active
./gradlew build      # first run decompiles Minecraft, then builds build/libs/agricrops-0.1.0.jar
./gradlew runClient  # launch a dev client
```

No submodules, no `setupDecompWorkspace`, no QuikMod — RFG handles the MCP
decompile/deobf as part of the build.

## What changed from the AgriCraft build

This fork migrated off AgriCraft's dead build stack (ForgeGradle 2.3-SNAPSHOT +
the QuikMod plugin + `http://` mavens that no longer exist):

- **RetroFuturaGradle** replaces ForgeGradle 2.x. Config lives in `build.gradle` /
  `settings.gradle` / `gradle.properties` (no auto-managed buildscript, no blowdryer).
- **Dependencies come from live repos**: GTNH Nexus, CurseMaven (pull any mod by
  CurseForge id via `rfg.deobf('curse.maven:slug-projectId:fileId')`), BlameJared,
  Maven Central.
- **InfinityLib** is now a prebuilt, deobfuscated dependency
  (`curse.maven:infinitylib-251396:3317119` — InfinityLib 1.12.1 for MC 1.12.2, the
  exact version the old submodule pinned) instead of a source submodule.
- **AgriCore** was vendored into the repo at `src/main/java/com/agricraft/agricore`
  (kept in its original `com.agricraft.*` namespace to mark the borrowed boundary,
  like InfinityLib). Per design §11/§12 its stat/genetics logic is slated to be
  replaced by a custom `CropStatCalculator`; the rest stays as-is for now. It pulls
  `org.reflections` (declared in `build.gradle`).
- **AgriPlants** (542 upstream default plant/mutation JSONs for other mods) was
  dropped — the fork defines its own species via the design's ZenScript-driven
  registries (§7/§11), so the upstream defaults are not carried over.
- **QuikMod token substitution is gone.** `Reference.java` and `mcmod.info` now hold
  plain literal values (mod id `agricrops`, package `dev.omega24.agricrops`, version
  `0.1.0`) instead of `${mod.x}` / `/*^...^*/` templates.

## Status

`./gradlew build` is **green** — it produces `build/libs/agricrops-0.1.0.jar` (the
reobfuscated mod jar) and `agricrops-0.1.0-dev.jar`. Verified with Temurin JDK 8.

Getting there needed three fixes beyond the scaffold (all committed):

1. **Mappings**: RFG defaults to `stable_39`, but the source was written against
   `snapshot_20180710` (set in the `minecraft {}` block). The wrong mappings rename
   methods (`createIcon`/`getUnlocalizedName`/...) and broke `@Override` across ~34
   files.
2. **Dependency coordinates**: Actually Additions / The One Probe / EnderIO now come
   from CurseMaven by file id (the original GAVs weren't on the live mavens).
3. **Two upstream latent bugs**: `scala.actors.threadpool.Arrays` imports (only
   compiled before because OpenComputers dragged Scala onto the classpath) → swapped
   to `java.util.Arrays`; and a missing `AgriCommands` import in `IProxy.java` that
   was already broken in upstream's tip commit `d889388a`.

**Runtime load is verified**: a headless `./gradlew runServer` reaches
`Done! For help` with `agricrops` loaded through all phases (Soils/Plants/Mutations
register 0/0 — empty, since the upstream default content was dropped). One runtime
fix was needed beyond the build: `org.reflections` was compile-only so it was
missing from the jar at load time; its sole use (AgriCore's default-resource jar
scan) was excised since the fork has no bundled defaults.

`runClient` (a visual client, needs the `shell.nix` FHS natives for a real display)
is the remaining manual checkpoint (getting-started §4).
