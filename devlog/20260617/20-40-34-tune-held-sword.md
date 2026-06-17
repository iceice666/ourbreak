# Devlog — 2026-06-17 20:40:34 — `tune-held-sword`

> **Author**: ceil
> **Build / Version**: feel — viewmodel polish
> **Branch / Commit**: feat/destruction-juice

---

## Summary

Tuned the first-person held sword: a smaller, properly 3D resting pose and a natural downward
slash instead of a flat plank that windmilled inward.

---

## What I worked on

- **Resting pose** (`WeaponModels.sword`): the blade was filling the screen and facing the eye flat
  (like a plank). Halved the model scale (0.85 → 0.5) and added a twist about the blade's own length
  (composed before the diagonal tilt) so we now see the blade in 3D — its thickness/edge with
  foreshortening — held hilt-low-right / blade-up-left like Minecraft's first-person sword.
- **Slash** (`HeldWeaponState`): the swing was a big roll about the view axis, so the blade swept
  around like a windmill from the outside inward. Replaced with a pitch-led downward diagonal chop
  (forward-and-down from the upper-left toward the lower-right), which reads as an actual slash.

## Why

Player feedback: the sword looked flat (not 3D), was too big, and the swing felt like it went "outer
to inner" — all about the viewmodel reading wrong, not gameplay.

## Tests / docs

- Cosmetic; headless logic untouched. `./gradlew test` green. Verified clean launch on Windows.

## Watch

- Pose/slash are pure tuning constants (twist angle, scale, swing euler) if further nudging is wanted.
