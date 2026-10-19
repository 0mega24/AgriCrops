# Agricrops

Agricrops is an experimental hard fork of AgriCraft for Minecraft Forge 1.12.2. Its goal is a
playable crop-breeding and field-management game: crops have inheritable genomes, respond to soil
and environment, cross into discoverable hybrids, consume field resources, and can become unstable
when pushed too far.

The intended product is **a standalone mod with optional pack-authoring tools**. A base Agricrops
installation must eventually provide a complete early-game loop and a small mutation progression.
CraftTweaker/ZenScript can extend or replace that content for modpacks, but it is not meant to be
required for ordinary play.

## Current status

This is pre-release development software. The Forge/RFG build works, all eight standard genes have
live gameplay consumers, fresh seeds use species templates, and the base jar ships its first
breeding route: Wheat + Beetroot can produce Winter Wheat with Frost Hardiness. Deterministic Java
tests exist, but the progression is small/unbalanced and the audit found trait propagation,
numeric-contract, player-explanation and migration gaps. Do not use this version for an
irreplaceable world. The external simulator was withdrawn on 2026-10-19; current design is an
all-Java mod with CraftTweaker custom content, native crop imports/art reuse, deeper breeding/soil
and in-game research/tools. These expanded mechanics are planned, not implemented here.

The proposed repository source of truth is [the product design](docs/product-design.md), pending
owner acceptance.
The audited implementation sequence and honest feature status are in the
[development roadmap](docs/roadmap/README.md). The latest owner-directed scope and links to the
vault design contracts are in [Decision 0002](docs/decisions/0002-java-in-game-scope.md).

## Build and development

Agricrops targets Java 8 and Minecraft Forge 1.12.2 using RetroFuturaGradle.

```sh
./gradlew build
./gradlew runServer
./gradlew runClient
```

On NixOS, enter `nix-shell` before starting the visual client. See [BUILDING.md](BUILDING.md) for
dependencies, build details, and known runtime checkpoints.

Java tests and in-game tools are the development/learning surfaces; no Node/npm product is required.

## Pack authoring

Pack authors can define soils, species, plants, mutations, locked attributes, and staged loot with
CraftTweaker. The current API and its limitations are documented in
[the pack-dev guide](crop-mod-packdev-guide.md), with working material in
[`zenscript-examples/`](zenscript-examples/). Reload behavior is not yet safe: duplicate registry
entries are currently retained rather than transactionally replaced.

## Project boundaries

- Minecraft Forge 1.12.2 and Java 8 are the supported game/runtime target.
- The base mod must remain useful without CraftTweaker or OpenComputers.
- Pack authoring extends the game; it does not excuse missing built-in progression.
- The external simulator is withdrawn. Player learning, inspection and author previews belong in
  the Java mod; deterministic Java tests verify the rule layer.
- Upstream AgriCraft compatibility is best-effort until a migration policy is explicitly shipped.

## License

See [LICENSE](LICENSE) and [NOTICE](NOTICE).
