export const statIds = [
  "growth", "yield", "resilience", "water_affinity", "fertility_response",
  "light_tolerance", "temp_tolerance", "mutation_chance"
] as const;

export type StatId = typeof statIds[number];
export type Genome = Record<StatId, number>;

export interface PlantDefinition {
  id: string;
  name: string;
  growthStages: number;
  baseStats: Genome;
}

export interface MutationDefinition {
  id: string;
  chance: number;
  child: string;
  parents: string[];
}

export interface ResolvedModelV1 {
  schema: "agricrops-model-v1";
  plants: PlantDefinition[];
  mutations: MutationDefinition[];
}

export interface Diagnostic {
  severity: "error" | "warning";
  message: string;
  line?: number;
}

const genome = (overrides: Partial<Genome> = {}): Genome => ({
  growth: 1, yield: 1, resilience: 1, water_affinity: 0,
  fertility_response: 1, light_tolerance: 0, temp_tolerance: 0,
  mutation_chance: 0, ...overrides
});

export const builtInModel: ResolvedModelV1 = {
  schema: "agricrops-model-v1",
  plants: [
    { id: "wheat", name: "Wheat", growthStages: 8, baseStats: genome() },
    { id: "beetroot", name: "Beetroot", growthStages: 4, baseStats: genome() },
    { id: "carrot", name: "Carrot", growthStages: 4, baseStats: genome() },
    { id: "winter_wheat", name: "Winter Wheat", growthStages: 8,
      baseStats: genome({ temp_tolerance: -8, resilience: 3 }) }
  ],
  mutations: [
    { id: "wheat_beetroot_to_winter_wheat", chance: 0.35,
      child: "winter_wheat", parents: ["wheat", "beetroot"] }
  ]
};

export function validateModel(model: ResolvedModelV1): Diagnostic[] {
  const diagnostics: Diagnostic[] = [];
  if (model.schema !== "agricrops-model-v1") {
    diagnostics.push({ severity: "error", message: "Unsupported model schema." });
  }
  const ids = new Set<string>();
  for (const plant of model.plants) {
    if (!plant.id || ids.has(plant.id)) {
      diagnostics.push({ severity: "error", message: `Duplicate or blank plant id: ${plant.id}` });
    }
    ids.add(plant.id);
    if (!Number.isInteger(plant.growthStages) || plant.growthStages < 2) {
      diagnostics.push({ severity: "error", message: `${plant.id}: growthStages must be an integer >= 2.` });
    }
    for (const stat of statIds) {
      const value = plant.baseStats[stat];
      if (!Number.isInteger(value) || value < -31 || value > 31) {
        diagnostics.push({ severity: "error", message: `${plant.id}.${stat} must be an integer in -31..31.` });
      }
    }
  }
  const mutationIds = new Set<string>();
  for (const mutation of model.mutations) {
    if (mutationIds.has(mutation.id)) {
      diagnostics.push({ severity: "error", message: `Duplicate mutation id: ${mutation.id}` });
    }
    mutationIds.add(mutation.id);
    if (!ids.has(mutation.child)) {
      diagnostics.push({ severity: "error", message: `${mutation.id}: unknown child ${mutation.child}.` });
    }
    if (mutation.parents.length < 2) {
      diagnostics.push({ severity: "error", message: `${mutation.id}: at least two parents are required.` });
    }
    for (const parent of mutation.parents) {
      if (!ids.has(parent)) diagnostics.push({ severity: "error", message: `${mutation.id}: unknown parent ${parent}.` });
    }
    if (mutation.chance < 0 || mutation.chance > 1) {
      diagnostics.push({ severity: "error", message: `${mutation.id}: chance must be in 0..1.` });
    }
  }
  return diagnostics;
}
