# Devlog — 2026-06-16 13:20:33 — `endless-survival-mode`

> **Author**: ceil
> **Build / Version**: endless survival pivot
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Reframed the fixed 4-round match into an endless survival run (player idea):
infinite rounds, escalating difficulty, score = rounds survived. Planned and
archived via OpenSpec (`endless-survival-mode`).

---

## What I worked on

### The meta-loop flip

- **Clear the wall → survive → next, harder round** (replaces the old instant
  WIN). **Timer expires with blocks standing → game over** at any round
  (replaces the round-4-only LOSS). No win state; score = round reached.
- `RoundComponent` drops `maxRounds`. `RoundSystem` no longer auto-advances on
  the timer — it just ticks — and gains `advanceRound()`. `VictorySystem`
  becomes the survival/progression system (clear → `advanceRound()`; timeout +
  blocks → LOSS).

### Escalating walls

- `NpcBuilderSystem` is now formula-driven for any round: count
  `min(16 + (r-1)*8, 48)` (16/24/32/40/48 then capped) and composition
  rounds 1–4 campaign, round 5+ the full gauntlet (ROCK/SHELL/JELLYFISH/CORAL).

### UI

- HUD shows `Round X` (no `/ 4`); `GameEndState` shows `GAME OVER — Reached
  Round N` + Restart; `GameplayState` passes the round reached on game over.

---

## Technical notes

- Retuned five tests to the endless rules (round/victory/npc/round-component/
  hud-text); `Result.WIN` is left in the enum as vestigial (no win in endless).
- Main specs updated: round-system (no cap, advance-on-survive), victory-system
  (survival + game-over), npc-building (formula + gauntlet), hud (round only).
- GDD/TDD rewritten: win/loss → survival + scoring; round structure → endless.

---

## Next session

- [ ] 3D stacked walls — blocks currently sit in one flat layer at the mascot's
  Y; stack them into a real fortress with height (player's next goal).
