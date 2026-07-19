# Building Agricrops

Agricrops is a Forge **1.12.2** mod, forked from AgriCraft. It targets **Java 8**
and builds with **ForgeGradle 2.3** via the third-party **QuikMod** Gradle plugin.

## Prerequisites

- **Java 8** (Forge 1.12.2 will not run on a newer JDK).
- **Git** (the project pulls three submodules — see below).
- On **NixOS**: use the provided [`shell.nix`](./shell.nix) (`nix-shell`) — ForgeGradle 2.x
  runs prebuilt native binaries that need an FHS layout. See `crop-mod-getting-started.md` §1.

## Submodules

The build depends on three git submodules under `lib/`:

| Path              | Repo                                            | Role                         |
|-------------------|-------------------------------------------------|------------------------------|
| `lib/AgriCore`    | `AgriCraft/AgriCore` (branch `1.12.2`)          | stat/genetics core (MIT)     |
| `lib/InfinityLib` | `InfinityRaider/InfinityLib` (branch `1.12.1`)  | base mod library (MIT)       |
| `lib/AgriPlants`  | `AgriCraft/AgriPlants`                          | default plant JSON defaults  |

Initialize them after cloning:

```sh
git submodule update --init --recursive
```

> Per the design (`crop-mod-design.md` §11 / §12), the intent is to **replace**
> AgriCore's stat/genetics logic with a custom `CropStatCalculator` rather than
> extend it — keep AgriCore only for non-stat utilities. That divergence hasn't
> started yet; AgriCore is currently still wired in as-is.

## Build

```sh
nix-shell                          # NixOS only; otherwise just ensure Java 8 is active
./gradlew setupDecompWorkspace     # one-time: decompiles Minecraft for the FG2 workspace
./gradlew build                    # produces the mod jar under build/libs/
./gradlew genIntellijRuns          # generate IntelliJ runClient / runServer configs
```

## Known build blocker: dead maven repositories

`build.gradle` still lists the `http://` maven repositories from AgriCraft's original
era. Several are dead or have moved to `https://`, so a clean `./gradlew build` will
fail resolving dependencies until they're updated. Suspect list:

- `http://files.minecraftforge.net/maven`  → `https://maven.minecraftforge.net/`
- `http://tehnut.info/maven`                (TheOneProbe — likely relocated)
- `http://maven.tterrag.com/`, `http://maven2.tterrag.com/`
- `http://dvs1.progwml6.com/files/maven`   (JEI — now under `https://maven.blamejared.com/`)
- `http://maven.cil.li/`                    (OpenComputers)
- `http://chickenbones.net/maven/`          (CodeChicken — dead)
- `http://maven.covers1624.net`             (Thermal — now `https://`)

Fixing these is the **first real task before any code work** (getting-started §3:
the build must be green, unmodified, before diverging). This has **not** been done
yet and could not be verified in the setup environment (no Java 8 there).

## How the mod id / package are wired (QuikMod)

`mod.properties` is the single source of truth. QuikMod substitutes its values into
the source at build time via inline `/*^${mod.x}^*/` tokens and `${mod.x}` template
markers. The important fields:

```
id      = agricrops              # the mod id (was: agricraft)
name    = Agricrops
package = dev.omega24.agricrops  # Java base package (hard-fork namespace; was: com.infinityraider.agricraft)
```

`Reference.MOD_ID` carries the id through the code (ResourceLocations, creative tabs,
unlocalized names). The `assets/agricrops/` resource domain must always match `MOD_ID`.

> The rename so far covers the **mod id** (lowercase `agricraft` → `agricrops`
> everywhere) and the **Java package**. Cosmetic mixed-case `AgriCraft` brand strings
> (the main `@Mod` class name, GUI/lang display text, config comments) were left as-is
> and can be de-branded later — they don't affect the mod id.
