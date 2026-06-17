# ourbreak — Milestone Breakdown

Derived from `gdd.md` + `tdd.md`. Sequences the TDD's component/system/test list
into a dependency-ordered, shippable roadmap for a **single final delivery**.

## Slicing philosophy

**Horizontal / test-first.** Logic systems are built and headless-tested first
(M1–M3), then wired into a runnable game (M4). Every milestone from M4 onward
leaves the project in a shippable state so a partial submission is still a
working game.

## Cross-cutting rules

| Rule | Detail |
|------|--------|
| TBD values never gate a system | Use named placeholder constants; tune all GDD `TBD` numbers in M7 |
| New deps need approval | Only M6 adds one (Lemur — ask before adding to `libs.versions.toml`) |
| Test gate | `./gradlew test` must pass before a milestone is "done" |
| Per-commit devlog | `/commit` skill + pre-commit hooks required on every commit |
| Package layout | Components → `ecs/components/`; systems/AppStates → `ecs/systems/`; tests → `app/src/test/java/com/ourbreak/` |

---

## M0 — Foundation ✅ *done*

**What exists:** jME3 + Zay-ES skeleton, `OurbreakGame`, `PositionComponent`,
`ModelComponent`, `ModelViewState`, `EntityDataTest`. Renders placeholder cubes.

**Shippable:** no (engine scaffold only).

---

## M1 — Core game loop (headless) ✅ *done*

**What exists:** `RoundComponent`, `PhaseComponent`, `GameResultComponent`,
the M1 `BlockComponent` stub, `RoundSystem`, and `VictorySystem`, covered by
`RoundSystemTest` and `VictorySystemTest`.

**Why first:** Every other system depends on knowing what phase/round it's in.
The victory/defeat conditions are simple boolean checks on this state — test them
before any content (blocks, weapons) exists.

**Lands (TDD §3.1, §3.2):**

| Artifact | Notes |
|----------|-------|
| `RoundComponent` | `currentRound`, `maxRounds` (singleton entity) |
| `PhaseComponent` | enum `BUILD \| ATTACK` |
| `RoundSystem` | BUILD→ATTACK switch; 60 s attack timer (`remainingSeconds`, clamp to 0); advance to next round; after round 4 → defeat |
| `VictorySystem` | ATTACK phase: all block-tagged entities cleared → immediate win; round 4 ends with blocks remaining → loss |

**Gating tests (TDD §10):** `RoundSystemTest`, `VictorySystemTest`
(use synthetic entities tagged with a `BlockComponent` stub for the count check).

**Depends on:** M0  
**Shippable:** no (headless logic only).

---

## M2 — Blocks & destruction (headless)

**Why here:** Block durability and the weapon counter-matrix are the core skill
mechanic. Building them headless-tested before the renderer is wired in keeps the
logic clean and verifiable.

**Lands (TDD §3.1, §3.2, §5, §6):**

| Artifact | Notes |
|----------|-------|
| `BlockComponent` | `blockType` (SAND/CORAL/SHELL/ROCK/JELLYFISH), `durability`, `maxDurability` |
| `MascotComponent` | Marker — no HP, win/loss decided by buildings |
| `WeaponComponent` | `weaponType` (SWORD/GUN/DRONE) on player entity |
| `WeaponSystem` | Attack input → damage application → durability decrement → destroy entity at 0; apply counter-matrix multipliers as named `float` constants (placeholder values — tuned in M7) |

**Counter-matrix constants (placeholder):**

| Weapon | Strong vs | Weak vs |
|--------|-----------|---------|
| SWORD | SAND | SHELL, CORAL |
| GUN | CORAL, JELLYFISH | ROCK |
| DRONE | ROCK, SAND | JELLYFISH, SHELL |

**Gating tests (TDD §10):** `BlockTest` (5 durabilities, overkill clamps to 0),
`WeaponTest` (hit damage + counter multipliers).

**Depends on:** M1  
**Shippable:** no (headless logic only).

---

## M3 — NPC builder (headless)

**Why here:** Completing the NPC script makes the full round (build → attack →
victory) runnable end-to-end headlessly — the logic completeness checkpoint
before any renderer work starts.

**Lands (TDD §3.2, §7):**

| Artifact | Notes |
|----------|-------|
| `NpcBuilderSystem` | BUILD phase: scripted block placement, priority front → left/right sides → outer ring; per-round composition table; detaches when all blocks placed, triggers BUILD→ATTACK |

**Per-round block composition (TDD §7):**

| Round | Block mix |
|-------|-----------|
| 1 | All SAND |
| 2 | SAND + CORAL |
| 3 | ROCK + SHELL |
| 4 | ROCK + JELLYFISH |

*(Block count per round is a `TBD` constant — use a placeholder, tune in M7.)*

**Gating tests (TDD §10):** `NpcBuilderTest` (each round places the correct
block types in the correct priority order).

**Depends on:** M2  
**Shippable:** no — but a complete round runs headlessly end-to-end.

---

## M4 — First-person playable ⭐ *Minimum Shippable Game*

**Why here:** Thin wiring milestone — wire M1–M3 logic to the renderer and
input layer. Goal is "complete game" ASAP; placeholder cubes, no effects, no HUD
polish. Everything after this is incremental polish on a working game.

**Lands (TDD §2, §3.2, §4):**

| Artifact | Notes |
|----------|-------|
| `MainMenuState` | Start Game, Exit buttons (placeholder 2D Swing or minimal jME UI — Lemur arrives in M6) |
| `GameplayState` | Root state that attaches/detaches Round/Victory/NpcBuilder/PlayerControl systems |
| `GameEndState` | Win / Lose display + Restart (→ MainMenuState) |
| `PlayerControlState` | WASD move, mouse-look capture, weapon switch (1/2/3 keys), left-click attack trigger → `WeaponSystem` |
| Raycast wiring | `WeaponSystem` raycast hits → `BlockComponent` durability; `ModelViewState` removes destroyed entity spatials |

**Tests:** Existing headless suite (`RoundSystemTest`, `VictorySystemTest`,
`BlockTest`, `WeaponTest`, `NpcBuilderTest`) stays green. Manual smoke test:
launch app, play a full 4-round match to both a win and a loss.

**Depends on:** M1 + M2 + M3  
**Shippable: YES** — first runnable build. NPC builds, player attacks under the
timer, win/lose resolves. No effects, unbalanced numbers — but a completable game.

---

## M5 — Block special effects

**Lands (TDD §3.1, §3.2, §5):**

| Artifact | Notes |
|----------|-------|
| `EffectComponent` | `effectType` marker on block entities |
| `BlockEffectSystem` | Coral: detect player within 1.5 cells → reduce move speed (placeholder % — tune M7); Shell: on-destroy → deal reflect damage to player; Jellyfish: on-placement → attach HUD post-process flicker filter; Drone: on-fire → 3×3 AoE damage application |

**Gating tests (TDD §10):** `BlockEffectTest` (Coral trigger condition, Shell
reflect trigger, Jellyfish trigger).

**Depends on:** M2 (blocks) + M4 (a live player entity to affect)  
**Shippable:** yes (tactical depth layer added).

---

## M6 — UI / HUD

**⚠️ New dependency: Lemur GUI** — not yet in `gradle/libs.versions.toml`.
Get explicit approval before adding (CLAUDE.md rule 7). Add as:
`implementation "com.simsilica:lemur:<version>"` in the version catalog.

**Lands (TDD §3.2, §9):**

| Artifact | Notes |
|----------|-------|
| `HudSystem` | Top-left: Round X / 4; top-right: attack countdown (ATTACK phase only); top-centre: remaining building count (ATTACK phase only) |
| `MainMenuState` (replace placeholder) | Lemur buttons: Start Game, Exit |
| `GameEndState` (replace placeholder) | Lemur panel: Win / Lose text + Restart button |

**Tests:** Existing suite green; manual UI walkthrough (menu → game → end screen → restart).

**Depends on:** M1 (round/timer/count data) + M4 (AppState machine)  
**Shippable:** yes (legible to a player — proper menus + HUD).

---

## M7 — Balancing pass

**Lands:** Replace every GDD `TBD` with a tuned value, and update the GDD/TDD
tables to match:

| Value | Location |
|-------|----------|
| Blocks-per-round count | `NpcBuilderSystem` constant, GDD §Mechanics |
| Coral slow % | `BlockEffectSystem` constant, GDD §Mechanics |
| Shell reflect damage | `BlockEffectSystem` constant, GDD §Mechanics |
| Jellyfish flicker duration | `BlockEffectSystem` constant, GDD §Mechanics |
| Sword / Gun / Drone base damage | `WeaponSystem` constants, GDD §Mechanics |
| Counter-matrix multiplier magnitudes | `WeaponSystem` constants |

**Tests:** Adjust `WeaponTest`, `BlockTest`, `BlockEffectTest` expectations to
the final constants; full suite green.

**Depends on:** M2 + M3 + M5 (all tunable systems exist and are playable)  
**Shippable:** yes (intended difficulty curve — the "feels right" build).

---

## M8 — Stretch (optional, time-permitting)

GDD explicitly marks these as "if development time is sufficient":

- **Playable builder / faction select** — player can choose Clawd, NPC becomes Openclaw, BUILD phase becomes player-controlled
- **Real 3D art** — Clawd, Openclaw, 5 block models, 3 weapon models replacing placeholder cubes
- **Visual effect polish** — particle FX for Coral/Shell/Drone, Jellyfish post-process refinement

**Depends on:** complete M4–M7 game  
**Shippable:** yes. **Cuttable without harming submission.**

---

## Critical-path summary

```
M1 → M2 → M3 → M4      ← critical path to a submittable game
                └─ M5   ← tactical depth
                └─ M6   ← UI polish (needs Lemur approval)
                └─ M7   ← balancing (depends on M5)
                └─ M8   ← stretch (optional)
```

**First submittable milestone: M4.**  
Every milestone from M4 onward keeps the game submittable — stop at any green
milestone and you have a working game.

---

## TDD coverage check

| TDD §10 test class | Gating milestone |
|--------------------|-----------------|
| `BlockTest` | M2 |
| `WeaponTest` | M2 |
| `BlockEffectTest` | M5 |
| `RoundSystemTest` | M1 |
| `VictorySystemTest` | M1 |
| `NpcBuilderTest` | M3 |

All 6 TDD test classes are covered. No invented work items.
