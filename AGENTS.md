# AGENTS.md

Guidelines for AI coding agents (Claude Code, Codex, Copilot, etc.) working in this repository.

---

## Project overview

**ourcraft** is a Java 21 project built with Gradle.  
License: MIT — see `LICENSE`.

---

## Environment

The dev environment is fully declared in `flake.nix` using Nix flakes.  
All commands below assume you are inside the dev shell.

```bash
# Enter the dev shell
nix develop

# Or automatically via direnv (add once, then it activates on cd)
echo 'use flake' > .envrc && direnv allow
```

Runtime: **Java 21** (`JAVA_HOME` is set automatically by the shell).  
Build tool: **Gradle** (wrapper preferred once scaffolded — use `./gradlew`, fall back to `gradle` if no wrapper yet).

---

## Common commands

| Task | Command |
|------|---------|
| Build | `./gradlew build` |
| Run tests | `./gradlew test` |
| Clean | `./gradlew clean` |
| Single test class | `./gradlew test --tests "com.example.FooTest"` |
| Dependency report | `./gradlew dependencies` |
| Format / lint | *(add tool here when configured)* |

> Before running any command, make sure the Nix dev shell is active (`nix develop`).

---

## Code conventions

- **Language level**: Java 21 — use modern features (records, sealed classes, pattern matching, text blocks) where they improve clarity.
- **Package root**: use `com.ourcraft` as the base package.
- **Formatting**: 4-space indentation, K&R braces, max line length 120.
- **Naming**: classes `UpperCamelCase`, methods/variables `lowerCamelCase`, constants `UPPER_SNAKE_CASE`.
- **Null handling**: prefer `Optional<T>` for return values that may be absent; avoid returning raw `null` from public APIs.
- **Tests**: JUnit 5 (`@Test` from `org.junit.jupiter.api`). One test class per production class, named `*Test`. Arrange–Act–Assert pattern.

---

## Repository layout (expected after scaffolding)

```
ourcraft/
├── flake.nix          # Nix dev environment
├── flake.lock         # Pinned Nix inputs — commit this
├── build.gradle(.kts) # Gradle build script
├── settings.gradle    # Project name / subprojects
├── gradlew            # Gradle wrapper (commit this)
├── src/
│   ├── main/java/com/ourcraft/   # Production code
│   └── test/java/com/ourcraft/   # Test code
└── AGENTS.md          # This file
```

---

## Current progress

Full milestone breakdown: `design/milestones.md`. Critical path: M1 → M2 → M3 → M4 (first shippable game).

| Milestone | Description | Status |
|-----------|-------------|--------|
| M0 | Foundation — jME3 + Zay-ES skeleton, ModelViewState | ✅ done |
| M1 | Core game loop — RoundSystem, VictorySystem (headless) | ✅ done |
| M2 | Blocks & destruction — BlockComponent, WeaponSystem, counter-matrix (headless) | ✅ done |
| M3 | NPC builder — NpcBuilderSystem, per-round scripts (headless) | ✅ done |
| M4 | First-person playable — AppState machine, PlayerControlState, raycast wiring | ✅ done |
| M5 | Block special effects — BlockEffectSystem, EffectComponent | ✅ done |
| M6 | UI / HUD — HudState, Lemur menus | ✅ done |
| M7 | Balancing — fill all TBD constants, retune tests | ⬜ next |
| M8 | Stretch — playable builder, real 3D art (optional) | ⬜ |

---

## Planning with OpenSpec (SDD)

This project uses **OpenSpec** as its Software Design Document (SDD) framework. Prefer SDD-driven workflows over the built-in plan mode.

- **When asked to plan or design anything**, default to OpenSpec skills (`/openspec-new-change`, `/opsx:new`, etc.) rather than entering built-in plan mode (`EnterPlanMode`).
- **If currently in plan mode** and the user asks you to plan or execute work: suggest they exit plan mode and use OpenSpec SDD instead, or confirm they want to use the built-in plan mode.
- **If an OpenSpec command/skill is invoked while in plan mode**: ask the user whether to exit plan mode before proceeding with OpenSpec.

OpenSpec skill entry points:
| Intent | Skill |
|--------|-------|
| Start a new change | `/openspec-new-change` or `/opsx:new` |
| Explore / think through an idea | `/openspec-explore` or `/opsx:explore` |
| Fast-forward all artifacts | `/openspec-ff-change` or `/opsx:ff` |
| Continue a change | `/openspec-continue-change` or `/opsx:continue` |
| Apply / implement tasks | `/openspec-apply-change` or `/opsx:apply` |
| Verify implementation | `/openspec-verify-change` or `/opsx:verify` |
| Archive a completed change | `/openspec-archive-change` or `/opsx:archive` |

---

## Agent rules

1. **Always work inside `nix develop`** — do not assume system Java or Gradle.
2. **Never commit generated build artefacts** — `*.class`, `*.jar`, `build/`, `.gradle/` are gitignored.
3. **Keep `flake.lock` committed** — it pins the exact toolchain; do not delete or regenerate it without good reason.
4. **Run tests before marking a task done** — `./gradlew test` must pass (zero failures, zero errors).
5. **Follow the `commit` skill for all commits** — see `.claude/skills/commit/SKILL.md` (invocable via `/commit`). It covers Conventional Commits format, the per-commit devlog requirement, and devlog immutability. The pre-commit hooks under `.claude/hooks/` enforce both the test gate and the devlog gate.
6. **Do not modify `flake.nix` / `flake.lock` unless explicitly asked** — toolchain changes are intentional.
7. **Ask before adding dependencies** — new Gradle dependencies need justification; prefer the Java standard library and well-known libraries (Guava, Jackson, JUnit 5).
8. **Update the Current Progress table on every commit** — when a milestone completes, flip its status to `✅ done` and mark the next milestone `⬜ next`. Stage `AGENTS.md` in the same commit as the milestone work.
