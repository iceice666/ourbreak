# Devlog — 2026-06-17 14:18:52 — `drone-blast-upgrade`

> **Author**: ceil
> **Build / Version**: gameplay — drone progression
> **Branch / Commit**: feat/destruction-juice

---

## Summary

The drone's blast radius now grows with the survival run, with a level readout on the HUD.

---

## What I worked on

- `WeaponSystem.droneLevelForRound(round) = 1 + (round-1)/3` — +1 level every 3 rounds, uncapped
  (user's call). Level n = a `(2n+1)²` blast: Lv1 3×3, Lv2 5×5, Lv3 7×7, …
- `BlockEffectSystem.droneAreaTargets(center, radius)` overload (the no-arg form stays radius 1 for
  the existing tests); `PlayerControlState` derives the radius from the current round on a drone attack.
- `DestructionFxState.explosion(center, blastRadius)` scales the fireball / smoke / shockwave ring /
  light by the blast diameter, so bigger levels detonate bigger.
- HUD shows `Weapon: DRONE  Lv.N` while the drone is equipped.
- Emergent tension (no extra code): a bigger AoE clears more but also more easily catches Jellyfish
  (poison) and Shells (split), so the upgrade raises reward *and* risk.

## Coverage

- `DroneLevelTest` (level steps every 3 rounds, uncapped, monotonic, rejects round < 1) and a 5×5
  radius case in `BlockEffectTest`. GDD/TDD updated.

## Next session

- [ ] Investigating: blocks reported as generating only around the single origin point — check
  `NpcBuilderSystem` placement.
