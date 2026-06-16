# Devlog — 2026-06-16 18:12:05 — `endless-difficulty-curve`

> **Author**: ceil
> **Build / Version**: M4–M5 playable effects, balance design
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Difficulty now rises forever (no round-5 plateau): the wall keeps growing every round,
smoothly and fairly, driven by a required-clear-rate model.

---

## What I worked on

### The problem

Difficulty ramped rounds 1→5 then flatlined — block count capped at 48, composition
fixed, time fixed at 60s. So past round 5 every wall was identical and the score was
pure endurance, not rising challenge.

### The model (math + psychology)

- **Required clear-rate as the difficulty axis**: `ρ(r) = blocks ÷ time`. The player has
  a max sustainable rate; design `ρ(r)` to approach it but never reach it.
- `NpcBuilderSystem`: rounds 1–4 keep the onboarding ramp (16/24/32/40). From round 5,
  `ρ(r) = 1.20 − 0.40·0.85^(r−5)` — rises asymptotically toward 1.20 with **shrinking
  increments** (×0.85/round), so it never spikes (Weber–Fechner) and, like a slowing
  divergent series, climbs **without bound yet never jumps**.
- `RoundSystem.attackSecondsForRound`: 60s through round 5, then +2s/round (unbounded),
  so the ever-bigger walls stay clearable.
- Block count `= round(ρ·time)`: 48 / 53 / 63 / 72 / 90 / 105 / 131 at r5/6/8/10/15/20/30
  — **always growing** (the wall keeps getting bigger), continuous with the old r5 = 48.
- Psychology: keeps the player in the flow channel (challenge just above skill); the
  asymptotic rate means walls are always *theoretically* clearable, so you lose to your
  own skill, not an unwinnable wall; bigger walls + more coral add novelty vs monotony.

### Coverage

- `DifficultyCurveTest`: onboarding + r5 continuity, blocks strictly increase forever,
  time grows past r5, increments never spike (≤ onboarding step), required rate stays
  below the asymptote (always survivable). GDD/TDD updated with the model and formulas.

## Next session

- [ ] Playtest-tune the constants (RATE_BASE/MAX/DECAY, +2s/round) for feel; consider
  shifting composition toward tankier blocks at very high rounds for cognitive variety.
