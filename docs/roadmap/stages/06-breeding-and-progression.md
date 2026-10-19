# Stage 6 — Breeding and progression

## Goal

Deliver the central playable loop: discover parents, make crosses, evaluate offspring, select
traits, stabilize lines, and unlock useful production.

## Current implementation

- Empty double crop sticks run mutation/spread strategies against mature neighbors.
- A registry can map parent plant IDs to a child and chance.
- Hard-incompatible two-parent pairs are filtered.
- A successful two-parent mutation can roll locked attributes.
- The base jar ships no mutation graph; example ZenScripts are not content.

## Work plan

Decision 0002 and the vault breeding contract supersede mature-only/linear-progression assumptions:

- [ ] Declare donor/recipient phase windows independently from render stages; make timing diagnosable.
- [ ] Design parent-species/backcross recovery cycles and genomic regression with explicit costs.
- [ ] Add juvenile clippings/rooting, crafted/bred origin rules and preserved reference lines.
- [ ] Validate locus lockout/recovery and acquisition-gate bypass across crosses/recipes/clippings.

- [ ] Design a small built-in progression graph with early, middle, and late-game objectives.
- [ ] Give every mutation a gameplay purpose, discovery clue, and reasonable acquisition path.
- [ ] Specify parent multiplicity, adjacency, maturity, environment, catalysts, and chance rules.
- [ ] Decide how the Mutation Chance stat affects offspring selection and/or formation probability.
- [ ] Support and define same-species breeding separately from cross-species mutation.
- [ ] Resolve semantics for mutations requiring more than two parents and their attributes.
- [ ] Add failed-cross feedback so players can distinguish bad parents, conditions, incompatibility,
  and unlucky rolls.
- [ ] Record mutation discoveries independently from species discoveries.
- [ ] Integrate recipes/progression with the actual target modpack, if pack-specific content is a
  product requirement.
- [ ] Add optional data inspection/debug commands that explain candidate mutations and roll inputs.

## Vertical-slice content target

- Three accessible parents with distinct habitat/resource strengths.
- One hybrid requiring deliberate field arrangement.
- One locked attribute with a visible benefit and a breeding cost.
- At least three generations needed to create a stable useful line.
- A journal path that moves from hints to confirmed parentage after discovery.

## Exit criteria

A new survival player can complete the vertical slice using in-game information, and the base jar
contains at least one meaningful, tested breeding progression without external scripts.

