# Fork archive and recovery

## Source snapshot

- Branch: `codex/archive-agricrops-fork-2026-10-19`
- Commit: `75d6f7baa52251ae7260156b4c23a0a4a9357dbb`
- Snapshot: all 880 tracked files after adding every non-ignored source/doc/test/companion file
  from the dirty working tree; 68 changed/added files relative to the preceding main commit.
- Main descends from this snapshot through an ordinary successor commit. No force reset, orphan
  branch or rewritten ancestry was used. The old source is absent from main's active tree.

Inspect a file without switching the checkout:

```sh
git show codex/archive-agricrops-fork-2026-10-19:README.md
git ls-tree -r --name-only codex/archive-agricrops-fork-2026-10-19
```

To work on the old project separately, create a normal worktree at a chosen separate path from
the archive branch. Do not replace a dirty new checkout to inspect historical files.

## Ignored local material

Git branches do not contain ignored build outputs, dependencies, logs or worlds. Those were moved
without deletion to `/home/slidy/rand/AgriCrops/.legacy-local/2026-10-19/`:

- `run/`: old development configuration, saves (including `New World`) and client crash report.
- `build/`: jars, Java test results/reports, old mod output and prepared Minecraft dependencies.
- `logs/` and `.gradle/`: previous logs/local project Gradle state.
- `companion-generated/`: node_modules, web distribution and emitted test JavaScript.
- `repository.bundle`: verified complete local Git recovery bundle, including the archive ref.

The local directory is ignored and not published. Keep a separate machine backup if protection
against disk failure is needed. It is not a versioned source dependency of the new project. Only
generated Minecraft/Forge dependencies may be reused for bounded local build verification; never
mix archived mod classes/resources with new sources or claim that reuse proves a clean build.

The archive branch and new main are local until explicitly pushed. No remote branch was deleted
or force-updated as part of this transition.
