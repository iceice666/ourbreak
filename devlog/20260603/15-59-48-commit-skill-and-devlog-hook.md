<!--
File path: devlog/20260603/15-59-48-commit-skill-and-devlog-hook.md
-->

# Devlog — 2026-06-03 15:59:48 — `commit-skill-and-devlog-hook`

> **Author**: brian1061225@gmail.com
> **Build / Version**: pre-commit infra
> **Branch / Commit**: main

---

## Summary

Stand up project-local commit infrastructure: a Conventional-Commit skill
(`/commit`), a pre-commit hook that requires a staged devlog for
`feat|fix|refactor|docs|build|perf` commits, and a Claude `settings.json`
that wires both the new devlog gate and the existing test gate as
`PreToolUse / Bash` hooks. Loosen the test hook so it no longer mandates Nix.

---

## What I worked on

### Feature: project commit skill — `.claude/skills/commit/SKILL.md`

- Holds the Conventional Commits format, devlog requirement matrix, devlog
  immutability rules, and project splitting guidance.
- Layered on top of the global `make-commit` skill (which covers generic
  staging/splitting mechanics).
- Invocable via `/commit`.

### Feature: devlog pre-commit hook — `.claude/hooks/pre-commit-devlog.sh`

- `PreToolUse / Bash`. Intercepts `git commit`, parses the conventional-commit
  type from `-m` / `--message`, and for
  `feat | fix | refactor | docs | build | perf` requires at least one staged
  file matching `^devlog/[0-9]{8}/[0-9]{2}-[0-9]{2}-[0-9]{2}-.+\.md$`.
- Skips `--amend` and types that don't require a devlog.
- Heredoc-aware fallback: scans each line for a CC prefix when shlex can't
  parse the command.
- Smoke-tested against six command variants (block / pass / skip).

### Wiring — `.claude/settings.json`

- New file. Registers the devlog gate (cheap check, runs first) then the test
  gate as PreToolUse hooks on `Bash`.
- The existing `Stop` devlog auto-writer (`.claude/hooks/devlog.sh`) is
  intentionally **not** wired — auto-generating a devlog at session end would
  collide with the immutability rule and produce unmanaged files.

### Refactor: AGENTS.md commit rules

- Rules 5–7 (Conventional Commits, devlog required, devlog immutability)
  removed and replaced with a single pointer to the commit skill.
- Remaining rules renumbered.

### Build: cross-platform test hook — `.claude/hooks/pre-commit-test.sh`

- Removed Nix-mandatory phrasing in the error message. Now suggests Nix /
  direnv alongside SDKMAN, Homebrew, scoop/choco, or a system package as
  ways to get Gradle onto PATH.
- Behavior unchanged: still prefers `./gradlew` and falls back to `gradle`.

---

## Decisions made

- **Decision**: Devlog required for `perf:` as well.
  **Reason**: User asked after initial draft — performance changes are worth
  recording (often have non-obvious motivation/numbers).
  **Alternatives considered**: leave `perf:` optional (initial draft).

- **Decision**: Do not auto-wire the existing `Stop` devlog hook.
  **Reason**: It writes a new devlog file at every session end. Wiring it
  would mean files appearing without the human curating slug / sections,
  and clashes with the immutability rule (the file would never be
  meaningfully filled in but would still be frozen on commit).
  **Alternatives considered**: wire it for convenience — rejected.

- **Decision**: One commit for the whole infra change, not split.
  **Reason**: AGENTS.md edit, new skill, new hook, settings wiring, and test
  hook tweak are all parts of the same intent: "set up project commit infra
  that works for any dev."
  **Alternatives considered**: split the cross-platform tweak as a separate
  `chore:` — rejected for tight coupling.

---

## Open questions / blockers

- [ ] Justfile recipe to verify required environment (gradle availability,
  python3 for the hook parser) — follow-up requested by user, will land in
  a separate commit.

## Next session

- [ ] Add the env-check Justfile recipe.
- [ ] Consider whether `test:` should also require a devlog when adding a
  whole new test suite vs. tweaking existing tests.
