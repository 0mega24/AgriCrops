import type { Diagnostic, MutationDefinition } from "./model.js";

export interface ZenParseResult {
  mutations: MutationDefinition[];
  diagnostics: Diagnostic[];
}

const mutationCall = /^\s*mods\.agricrops\.Mutations\.addMutation\(\s*"([^"]+)"\s*,\s*([0-9.]+)\s*,\s*"([^"]+)"\s*,\s*\[([^\]]*)\]\s*\)\s*;?\s*$/;

export function parseZenMutations(source: string): ZenParseResult {
  const mutations: MutationDefinition[] = [];
  const diagnostics: Diagnostic[] = [];
  source.split(/\r?\n/).forEach((line, index) => {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("//") || trimmed.startsWith("#")) return;
    const match = mutationCall.exec(line);
    if (!match) {
      diagnostics.push({ severity: "warning", line: index + 1,
        message: "Unsupported ZenScript statement; v1 accepts declarative Mutations.addMutation calls only." });
      return;
    }
    const parents = [...match[4].matchAll(/"([^"]+)"/g)].map(parent => parent[1]);
    mutations.push({ id: match[1], chance: Number(match[2]), child: match[3], parents });
  });
  return { mutations, diagnostics };
}
