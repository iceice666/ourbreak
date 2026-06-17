# Devlog — 2026-06-16 12:15:47 — `m7-balancing`

> **Author**: ceil
> **Build / Version**: M7 — Balancing
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

M7: replaced every placeholder constant and GDD `TBD` with a deliberately
designed value set that turns the four rounds into an escalating weapon-rotation
puzzle. Planned and archived via OpenSpec (`m7-balancing`).

---

## What I worked on

### The design — four-round weapon-rotation puzzle

Difficulty comes from block *composition + effects*, not bigger numbers. Each
round has a "correct" weapon read; the wrong choice is punished.

- **Per-weapon base damage** (Sword 1.0 / Gun 2.0 / Drone 1.0) × counter matrix
  (strong ×2 / weak ×0.5 / neutral ×1) → hits-to-kill that give each weapon a
  real identity: Sword = fast melee (king of Sand), Gun = safe precise ranged
  (neutralises effect blocks, weak vs Rock), Drone = rock-breaker/crowd-clearer
  (but weak vs Shell/Jelly).
- R1 Sand (tutorial) → R2 Coral (range beats slow) → R3 Rock+Shell (sequence
  before you AoE, or eat chain reflect) → R4 Rock+Jelly (kill the vision
  disruptors first, then commit the Drone). Same 60 s clock throughout.

### Constants

- `WeaponSystem`: `SWORD/GUN/DRONE_BASE_DAMAGE` replace the single `BASE_DAMAGE`.
- `BlockEffectSystem`: Coral slow 0.5, Shell reflect 20, new
  `JELLYFISH_FLICKER_SECONDS = 2.0` (reserved for the flicker visual).
- `GameplayState.PLAYER_MAX_HEALTH` 10 → 100. Blocks-per-round confirmed at 8.
- `design/gdd.md` + `design/tdd.md` mechanics tables filled with tuned values.

---

## Decisions made

- **Decision**: per-weapon base damage instead of one shared base.
  **Reason**: without it the three weapons play identically; per-weapon base is
  what makes the counter-matrix read matter.
- **Decision**: keep 8 blocks/round (one ring), escalate via composition.
  **Reason**: a single readable wall; difficulty from *what* it is, not *how
  thick*.

---

## Open questions / blockers

- [ ] **Shell reflect + player health have no consequence yet.** The 100-point
  bar drains but nothing happens at 0. Strongly recommend a follow-up: a brief
  stun / control-loss (~2 s of lost attack time) on 0 HP — NOT death (the GDD
  keeps win/loss on buildings + timer). M7 tuned the numbers so this drops in
  cleanly; it does not add the mechanic (out of scope).
- [ ] Balance is reasoned but not deeply playtested — needs play to confirm R4
  is beatable in 60 s under vision disruption. All values are named constants.

---

## Next session

- [ ] Playtest the balance; consider the reflect-consequence follow-up.
- [ ] M8 (stretch, optional): playable builder / faction select, real 3D art,
  particle FX, Sword 3×1 sweep + weapon ranges (deferred mechanics).
