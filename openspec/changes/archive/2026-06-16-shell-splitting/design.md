## Context

Shell currently reflects 20 damage to a 100-point `PlayerHealthComponent` on destruction — but nothing happens when that bar empties (M5 decoupled it from win/loss and no death rule was ever added), so the Shell counter is inert and the Drone has no real weakness. The player and co-designer chose to drop player health and make Shell defend itself by **multiplying**: hit it wrong and it splits, so a Drone-spammed Shell wall snowballs out of control and the survival clock kills you. The Gun is the clean answer.

## Goals / Non-Goals

**Goals:**
- Remove player health and the reflect model.
- Sword/Drone destroying a Shell → spawn 2 new Shells (uncapped cascade); Gun → clean kill.
- Make the Drone non-trivial: AoE through Shells is self-defeating.

**Non-Goals:**
- Bounding or "fragment-debris" the cascade — the uncapped split is the intended punishment.
- Touching other blocks' durability/damage or the counter-matrix numbers.
- Jellyfish vision / Sword sweep / grounded movement (separate follow-ups).

## Decisions

### D1 — Split is resolved in `WeaponSystem` (it knows the weapon)
The split depends on which weapon struck, which only the attack path knows, so `WeaponSystem.attack` special-cases Shell: when the equipped weapon is SWORD or DRONE and the target is a Shell, the Shell is removed and **2 new Shell entities are spawned**; when the weapon is GUN, the Shell is destroyed normally. To place the fragments, `WeaponSystem` holds an occupancy view (an `EntitySet` of positioned blocks) and searches outward from the Shell's cell for the nearest empty cells (same grid as the NPC wall: XZ neighbours then upward), with a `close()` to release the set. Rationale: keeps the weapon-aware logic in one place; reuses the existing integer grid. Alternative: a separate post-destroy system — rejected because the weapon isn't recorded at destruction time.

### D2 — Fragments are full Shells, uncapped
A split spawns 2 `BlockComponent(SHELL)` entities (with `PositionComponent`, `ModelComponent`, `EffectComponent`), each able to split again. No generation cap. Rationale: the co-designer wants the runaway specifically so mindless Drone use is punished — a careless AoE can fill the arena with Shells and lose the round. Alternative: inert debris that can't re-split — rejected (too forgiving; doesn't deter Drone-spam).

### D3 — Remove player health rather than repurpose it
Delete `PlayerHealthComponent`, `PLAYER_MAX_HEALTH`, `SHELL_REFLECT_DAMAGE`, and the reflect tracking in `BlockEffectSystem`. Rationale: with the split as Shell's consequence, health is dead weight, and the GDD already says the player has no HP. `EffectComponent` keeps the SHELL→(marker) mapping for queryability; the counter-matrix is untouched (Sword/Drone "weak vs Shell" now reads as "triggers split").

## Risks / Trade-offs

- **Uncapped cascade could fill the arena and soft-lock a round** → Accepted/by design: that *is* the punishment for AoE-ing Shells; the round ends in a normal game-over when the timer runs out. The only safety need is that the spawn search always finds cells (the grid is unbounded outward/upward, so it always can).
- **Spawn placement when the area is dense** → Mitigation: search rings outward and layers upward from the Shell (the wall grid), guaranteeing empty cells exist; pick the 2 nearest.
- **Drone AoE hits several Shells at once → many simultaneous splits** → Accepted: that is the intended snowball; `WeaponSystem` already processes each target, so each Shell in the blast splits.
- **Balance is now swingy** (one bad Drone volley can spiral) → Mitigation: all knobs are constants (split count = 2); verified by playtest, easy to retune (e.g., to 1) if it feels unfair.
