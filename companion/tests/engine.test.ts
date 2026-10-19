import test from "node:test";
import assert from "node:assert/strict";
import { builtInModel, validateModel } from "../src/model.js";
import { createPlot, environmentFactor, SeededRandom, stepPlot } from "../src/engine.js";
import { parseZenMutations } from "../src/zenscript.js";

test("seeded plot replay is deterministic", () => {
  const first = createPlot(builtInModel.plants[0]);
  const second = createPlot(builtInModel.plants[0]);
  const a = new SeededRandom(99); const b = new SeededRandom(99);
  const traceA = Array.from({ length: 40 }, () => stepPlot(builtInModel, first, a));
  const traceB = Array.from({ length: 40 }, () => stepPlot(builtInModel, second, b));
  assert.deepEqual(traceA, traceB); assert.deepEqual(first, second);
});

test("cold adaptation beats warm adaptation in a cold biome", () => {
  const neutral = builtInModel.plants[0].baseStats;
  assert.ok(environmentFactor({ ...neutral, temp_tolerance: -31 }, 12, -0.4)
    > environmentFactor({ ...neutral, temp_tolerance: 31 }, 12, -0.4));
});

test("model and supported ZenScript mutation validate", () => {
  assert.deepEqual(validateModel(builtInModel), []);
  const parsed = parseZenMutations('mods.agricrops.Mutations.addMutation("m", 0.5, "winter_wheat", ["wheat", "beetroot"]);');
  assert.equal(parsed.diagnostics.length, 0); assert.equal(parsed.mutations[0].parents.length, 2);
});
