# Agricrops genome specification v0.1

This document describes the implemented genome contract used by the first vertical slice. It is
the source for future resolved-model and companion-simulator fixtures; balance constants may still
change before 1.0.

## Standard values

Every genome stores eight signed bytes clamped to `-31..31`. Growth, Yield, Resilience, Water
Affinity, Fertility Response, and Mutation Chance are magnitude traits: negative values contribute
zero until a specific negative mechanic is designed. Light Tolerance and Temperature Tolerance
are bipolar adaptations: zero is a generalist midpoint, negative moves the preferred condition
dimmer/colder, and positive moves it brighter/warmer.

Fresh seeds are constructed through `GenomeFactory` from their registered species template and
start unanalyzed unless a caller explicitly requests otherwise. Missing species definitions fail
to a safe all-zero wild type. Loaded and inherited genomes retain their values but pass through the
same seed-construction boundary.

## Environment

Light Tolerance shifts preferred block light from a neutral 12 by up to four levels. Temperature
Tolerance shifts preferred biome temperature from 0.8 by up to 0.8. Distance from either preferred
condition lowers the combined environment factor to a floor of 0.4. The factor multiplies soil
nutrient, moisture, biome/elevation, and base genetic growth factors.

## Crossing and randomness

Mutation and same-species spread use only the `Random` supplied by the world/caller. With identical
inputs and PRNG seed, inheritance is repeatable. Standard values currently blend mature valid
parents using the selected normal/hardcore calculator; replacing this inherited arithmetic with
explicit per-stat policies is still planned.

## Locked attributes

A locked attribute contains two value/dominance alleles and a bred-true generation count. When
dominance differs, the dominant allele expresses; when it matches, the average expresses. A child
keeps an attribute only if its species declares the matching slot.

A registered parent pairing can unlock a new attribute. Fresh unlocks survive their first
generation. Later crosses in which only one parent carries an unstable attribute have a 50% seeded
dropout roll. If both parents carry it, the offspring receives one random allele from each and its
bred-true count becomes the lower parent count plus one. At or above the attribute stabilization
threshold, a single-carrier copy no longer drops out.

`frost_hardiness` is the first mechanical hook. Its positive expressed value reduces cold-side
temperature mismatch by up to 75%, without providing a warm-biome bonus. Wheat + Beetroot can
produce Winter Wheat and guarantees the trait unlock when the mutation succeeds; Winter Wheat is
the only built-in species with that slot.
