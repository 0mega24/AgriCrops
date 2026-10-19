import type { Genome, PlantDefinition, ResolvedModelV1 } from "./model.js";

export interface PlotState {
  plantId: string;
  stage: number;
  moisture: number;
  nutrients: number;
  light: number;
  temperature: number;
  genome: Genome;
  elapsedTicks: number;
}

export interface SimulationEvent {
  tick: number;
  type: "growth" | "stress" | "mature";
  message: string;
  chance: number;
  roll: number;
}

export class SeededRandom {
  private state: number;
  constructor(seed: number) { this.state = seed >>> 0; }
  next(): number {
    this.state += 0x6D2B79F5;
    let value = this.state;
    value = Math.imul(value ^ value >>> 15, value | 1);
    value ^= value + Math.imul(value ^ value >>> 7, value | 61);
    return ((value ^ value >>> 14) >>> 0) / 4294967296;
  }
}

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value));

export function environmentFactor(genome: Genome, light: number, temperature: number, frostHardiness = 0): number {
  const lightPreference = 12 + genome.light_tolerance / 31 * 4;
  const lightFit = clamp(1 - Math.abs(clamp(light, 0, 15) - lightPreference) / 12, 0, 1);
  const temperaturePreference = 0.8 + genome.temp_tolerance / 31 * 0.8;
  let mismatch = Math.abs(temperature - temperaturePreference);
  if (temperature < temperaturePreference) {
    mismatch *= 1 - 0.75 * clamp(frostHardiness / 31, 0, 1);
  }
  const temperatureFit = clamp(1 - mismatch / 1.5, 0, 1);
  return 0.4 + 0.6 * ((lightFit + temperatureFit) / 2);
}

export function growthChance(plot: PlotState): number {
  const growth = Math.max(0, plot.genome.growth);
  const base = 0.05 + growth * 0.05;
  const fertility = Math.max(0, plot.genome.fertility_response) / 31;
  const nutrientFloor = 0.5 + 0.5 * fertility;
  const nutrientFactor = nutrientFloor + (1 - nutrientFloor) * clamp(plot.nutrients / 100, 0, 1);
  const affinity = Math.max(0, plot.genome.water_affinity) / 31;
  const moistureFactor = 1 + (clamp(plot.moisture / 100, 0, 1) - 0.5) * affinity;
  return clamp(base * nutrientFactor * moistureFactor
    * environmentFactor(plot.genome, plot.light, plot.temperature), 0, 1);
}

export function createPlot(plant: PlantDefinition): PlotState {
  return { plantId: plant.id, stage: 0, moisture: 50, nutrients: 100, light: 12,
    temperature: 0.8, genome: { ...plant.baseStats }, elapsedTicks: 0 };
}

export function stepPlot(model: ResolvedModelV1, plot: PlotState, random: SeededRandom): SimulationEvent {
  const plant = model.plants.find(candidate => candidate.id === plot.plantId);
  if (!plant) throw new Error(`Unknown plant ${plot.plantId}`);
  plot.elapsedTicks += 1;
  plot.moisture = Math.max(0, plot.moisture - 0.35);
  const chance = growthChance(plot);
  const roll = random.next();
  if (plot.stage >= plant.growthStages - 1) {
    return { tick: plot.elapsedTicks, type: "mature", message: `${plant.name} is mature.`, chance, roll };
  }
  if (roll < chance) {
    plot.stage += 1;
    plot.nutrients = Math.max(0, plot.nutrients - 2);
    return { tick: plot.elapsedTicks, type: plot.stage === plant.growthStages - 1 ? "mature" : "growth",
      message: `${plant.name} advanced to stage ${plot.stage + 1}/${plant.growthStages}.`, chance, roll };
  }
  return { tick: plot.elapsedTicks, type: "stress",
    message: `No growth: chance ${(chance * 100).toFixed(1)}%, roll ${(roll * 100).toFixed(1)}%.`, chance, roll };
}
