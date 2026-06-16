## Why

The current game is a 4-round match you "win" the instant you clear any round's wall — a weak, one-and-done goal with no replay value. Reframing it as an **endless survival run** — infinite rounds, escalating difficulty, score = rounds survived — turns the same systems into a high-score chase: each round you clear pushes you into a harder one, and you play to beat your best.

## What Changes

- **No win, only how far you get.** Clearing every block in an ATTACK phase no longer wins the game — it means you **survived the round** and advances you to the next, harder round. The run ends only when an ATTACK timer expires with blocks still standing → **game over**, with your score being the round you reached.
- **Infinite, escalating rounds.** Remove the 4-round cap. The NPC's wall grows and gets nastier each round via formulas (block count ramps then caps; composition escalates to the full block gauntlet), so difficulty rises round over round instead of stopping at 4.
- **Round flow inverts**: advance-on-clear (success) replaces advance-on-timeout; timeout-with-blocks is now the failure at *any* round, not just round 4.
- **HUD** shows `Round X` (no `/ 4`); the **end screen** shows `GAME OVER — Reached Round N` + Restart.
- **GDD/TDD** updated to describe the endless survival loop, scoring, and the escalation formulas, replacing the old fixed-4-round win/loss rules.

## Capabilities

### Modified Capabilities

- `round-system`: Rounds are unbounded; the attack timer no longer auto-advances the round, and the round advances on a survived (cleared) round instead.
- `victory-system`: Becomes survival/progression — clearing a wall advances the round; an attack timeout with blocks remaining ends the run (game over) at any round; there is no win state.
- `npc-building`: Per-round block count and composition are formula-driven for an arbitrary round number (escalating), not a hardcoded 1–4 table.
- `hud`: The round readout shows the current round only (no maximum).

## Impact

- `RoundComponent` drops `maxRounds`; `RoundSystem`, `VictorySystem` (survival), `NpcBuilderSystem` (formulas), `HudText`/`HudState`, `GameEndState`, and `GameplayState` change accordingly.
- Retunes `RoundSystemTest`, `VictorySystemTest`, `NpcBuilderTest`, `RoundComponentTest`, `HudTextTest` to the endless rules.
- Updates `design/gdd.md` (win/loss → survival + scoring; round structure) and `design/tdd.md`.
- No new dependencies. The block effects, weapon damage, and M7 tuning are unchanged — only the round meta-loop and difficulty scaling change.
