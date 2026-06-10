<!--
File path: devlog/20260610/09-32-11-m2-blocks-destruction.md
-->

# Devlog — 2026-06-10 09:32:11 — `m2-blocks-destruction`

> **Author**: iceice666
> **Build / Version**: M2
> **Branch / Commit**: main

---

## Summary

Implemented headless block durability and weapon damage resolution, including
the complete counter matrix, phase gating, target validation, and destruction.
Archived the completed OpenSpec change and synced both capabilities.

---

## Goals for this session

- [x] Add typed block, weapon, and mascot ECS components
- [x] Implement deterministic weapon damage and block destruction
- [x] Cover durability, counters, phase gating, and target handling
- [x] Pass the complete test suite
- [x] Archive M2 and sync its main specs

---

## What I worked on

### Feature / System: `BlockComponent`

- Added all five block types with standard durability values
- Added full-health construction and immutable damage updates
- Validated durability state and clamped overkill damage to zero

### Feature / System: `WeaponSystem`

- Added SWORD, GUN, and DRONE weapon types
- Applied named base, strong, weak, and neutral damage constants
- Enforced ATTACK-phase eligibility and required a player weapon
- Deduplicated preselected targets and ignored missing or non-block entities
- Replaced damaged block state or removed entities on lethal damage

### Tests and specifications

- Added `BlockTest` and `WeaponTest`, bringing the suite to 28 passing tests
- Updated M1 victory fixtures to use typed full-health blocks
- Synced `block-durability` and `weapon-damage` into the main OpenSpec specs
- Archived the completed change as `2026-06-10-m2-blocks-destruction`

---

## Decisions made

- **Decision**: Accept preselected entity IDs in `WeaponSystem`.
  **Reason**: Keeps M2 headless and leaves raycast and area selection for M4.
  **Alternatives considered**: Implement weapon geometry in M2, which would
  couple domain damage rules to rendering and input.

- **Decision**: Remove a block entity when durability reaches zero.
  **Reason**: Preserves the existing `VictorySystem` contract that counts
  remaining `BlockComponent` entities.
  **Alternatives considered**: Retain a zero-health entity with a destroyed
  marker, which would require changing M1 victory behavior.

---

## Technical notes

- Damage is `BASE_DAMAGE * counter multiplier`.
- Each target is processed at most once per attack.
- Standard durability: SAND 1, CORAL 2, SHELL 1, ROCK 4, JELLYFISH 1.
- No dependencies, rendering code, physics, input, or targeting geometry were
  added.

---

## Next session

- [ ] Start M3: `NpcBuilderSystem` and per-round build scripts
