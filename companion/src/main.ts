import "./style.css";
import { builtInModel, validateModel, type ResolvedModelV1 } from "./model.js";
import { createPlot, SeededRandom, stepPlot, type PlotState, type SimulationEvent } from "./engine.js";
import { parseZenMutations } from "./zenscript.js";

const app = document.querySelector<HTMLElement>("#app")!;
let model: ResolvedModelV1 = structuredClone(builtInModel);
let plot: PlotState = createPlot(model.plants[0]);
let random = new SeededRandom(424242);
let events: SimulationEvent[] = [];

app.innerHTML = `
  <header><p class="eyebrow">LOCAL AGRONOMY WORKBENCH</p><h1>Agricrops Field Lab</h1>
    <p>Validate a resolved crop model, inspect the first breeding path, and replay a seeded plot.</p></header>
  <section class="status"><span class="pulse"></span><strong>Exact v0.1 core formulas · default config</strong><span>Files stay in this browser.</span></section>
  <div class="workspace">
    <section class="panel source"><div class="panel-title"><h2>Pack model</h2><button id="load-example">Reset example</button></div>
      <textarea id="model-source" spellcheck="false"></textarea><div id="diagnostics" class="diagnostics"></div></section>
    <section class="panel lab"><div class="panel-title"><h2>Single plot</h2><label>Seed <input id="seed" type="number" value="424242"></label></div>
      <div class="controls"><label>Crop <select id="plant"></select></label><label>Light <input id="light" type="range" min="0" max="15" value="12"><output id="light-out">12</output></label>
      <label>Temperature <input id="temperature" type="range" min="-5" max="20" value="8"><output id="temperature-out">0.8</output></label></div>
      <div class="plot-card"><div class="crop-mark" id="crop-mark">I</div><div><p id="crop-name"></p><strong id="stage"></strong><p id="resources"></p></div></div>
      <div class="actions"><button id="step">Step tick</button><button id="hundred">Run 100</button><button id="reset">Reset plot</button></div>
      <ol id="events" class="events"></ol></section>
    <section class="panel graph"><div class="panel-title"><h2>Breeding path</h2><span id="model-count"></span></div><div id="graph"></div>
      <details><summary>Supported ZenScript preview</summary><textarea id="zen-source">mods.agricrops.Mutations.addMutation("wheat_beetroot_to_winter_wheat", 0.35, "winter_wheat", ["wheat", "beetroot"]);</textarea><div id="zen-result"></div></details></section>
  </div>`;

const source = document.querySelector<HTMLTextAreaElement>("#model-source")!;
const plantSelect = document.querySelector<HTMLSelectElement>("#plant")!;

function refreshModel(): void {
  try {
    const candidate = JSON.parse(source.value) as ResolvedModelV1;
    const diagnostics = validateModel(candidate);
    document.querySelector("#diagnostics")!.innerHTML = diagnostics.length
      ? diagnostics.map(item => `<p class="${item.severity}">${item.message}</p>`).join("")
      : `<p class="ok">Model valid — ${candidate.plants.length} plants, ${candidate.mutations.length} mutation.</p>`;
    if (!diagnostics.some(item => item.severity === "error")) { model = candidate; renderDefinitions(); }
  } catch (error) {
    document.querySelector("#diagnostics")!.innerHTML = `<p class="error">${(error as Error).message}</p>`;
  }
}

function renderDefinitions(): void {
  const selected = plantSelect.value;
  plantSelect.innerHTML = model.plants.map(plant => `<option value="${plant.id}">${plant.name}</option>`).join("");
  if (model.plants.some(plant => plant.id === selected)) plantSelect.value = selected;
  document.querySelector("#model-count")!.textContent = `${model.plants.length} plants · ${model.mutations.length} route`;
  document.querySelector("#graph")!.innerHTML = model.mutations.map(mutation => `<div class="mutation"><div>${mutation.parents.map(id => `<span>${id}</span>`).join("<b>+</b>")}</div><i>↘ ${(mutation.chance * 100).toFixed(0)}%</i><strong>${mutation.child}</strong></div>`).join("");
}

function resetPlot(): void {
  const plant = model.plants.find(item => item.id === plantSelect.value) ?? model.plants[0];
  plot = createPlot(plant); events = [];
  plot.light = Number((document.querySelector("#light") as HTMLInputElement).value);
  plot.temperature = Number((document.querySelector("#temperature") as HTMLInputElement).value) / 10;
  random = new SeededRandom(Number((document.querySelector("#seed") as HTMLInputElement).value));
  renderPlot();
}

function run(ticks: number): void { for (let i = 0; i < ticks; i++) events.unshift(stepPlot(model, plot, random)); renderPlot(); }
function renderPlot(): void {
  const plant = model.plants.find(item => item.id === plot.plantId)!;
  document.querySelector("#crop-name")!.textContent = plant.name;
  document.querySelector("#stage")!.textContent = `Stage ${plot.stage + 1} / ${plant.growthStages}`;
  document.querySelector("#resources")!.textContent = `Moisture ${plot.moisture.toFixed(0)} · Nutrients ${plot.nutrients.toFixed(0)} · Tick ${plot.elapsedTicks}`;
  document.querySelector("#crop-mark")!.textContent = "I".repeat(Math.max(1, Math.ceil((plot.stage + 1) / 2)));
  document.querySelector("#events")!.innerHTML = events.slice(0, 8).map(event => `<li class="${event.type}"><span>${event.tick}</span>${event.message}</li>`).join("");
}

source.addEventListener("input", refreshModel);
document.querySelector("#load-example")!.addEventListener("click", () => { source.value = JSON.stringify(builtInModel, null, 2); refreshModel(); });
document.querySelector("#step")!.addEventListener("click", () => run(1));
document.querySelector("#hundred")!.addEventListener("click", () => run(100));
document.querySelector("#reset")!.addEventListener("click", resetPlot);
plantSelect.addEventListener("change", resetPlot);
for (const id of ["light", "temperature"] as const) document.querySelector(`#${id}`)!.addEventListener("input", event => {
  const value = Number((event.target as HTMLInputElement).value); const shown = id === "temperature" ? (value / 10).toFixed(1) : String(value);
  document.querySelector(`#${id}-out`)!.textContent = shown; if (id === "light") plot.light = value; else plot.temperature = value / 10;
});
document.querySelector("#zen-source")!.addEventListener("input", event => {
  const result = parseZenMutations((event.target as HTMLTextAreaElement).value);
  document.querySelector("#zen-result")!.textContent = `${result.mutations.length} supported call(s), ${result.diagnostics.length} diagnostic(s)`;
});

source.value = JSON.stringify(model, null, 2); refreshModel(); resetPlot();
