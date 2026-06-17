## Context

The Jellyfish effect has always been deferred — `BlockEffectSystem` only counts a flicker trigger on placement, nothing renders it — so "Gun/Sword beats Jellyfish / Drone loses to Jellyfish" is invisible. Earlier passes tried a screen flicker (no felt impact) and a drone-gated decoy hallucination (still weak). The Sword's GDD "3×1 sweep" was never built, so Sword and Gun play the same (single raycast) bar a damage number. Both are now buildable on top of the working grid + crosshair-pick.

## Goals / Non-Goals

**Goals:**
- Droning a Jellyfish poisons the player (+5s, cap 10s); Gun/Sword kills don't.
- While poisoned, real blocks become type-unreadable (random rainbow flicker), any weapon; a draining bar shows the remaining poison.
- Sword attacks clear a 3-cell horizontal row across the player's view.
- Headless coverage for the row expansion; the poison hallucination verified by launch.

**Non-Goals:**
- Player health / damage-over-time from poison (poison only blinds, it doesn't hurt).
- Bullet physics / true light occlusion; the recolour is per-geometry material swapping.
- Re-tuning damage/durability/counts.

## Decisions

### D1 — Jellyfish disruption is a poison timer triggered only by drone-killing a Jellyfish
A new `PoisonState` (`BaseAppState`, attached by `GameplayState`) holds an `EntitySet` of blocks and a 0–10s poison timer. Each frame it diffs the block set: a removed Jellyfish whose kill frame had the DRONE equipped adds +5s (capped at 10s) — Gun/Sword kills add nothing. The timer drains by `tpf` each frame. While it is above zero, **regardless of the equipped weapon**, every real block geometry (the scene children tagged with the `entityId` user-data) is recoloured to a random rainbow colour, re-scrambled ~every 0.12s, so the player can't read block types; when it hits zero each block is restored via `ModelViewState.colorFor(geom.getName())`. A draining bar on the GUI node shows `poison / 10s`. Rationale: a plain flicker and even decoy cubes had no felt impact, and the real design problem was that area-droning the wall was mindless; making the **drone the only thing that poisons you**, and the poison **blind you to block types** (so you can't safely keep droning), directly punishes lazy area-fire while leaving Gun/Sword as the clean answer to Jellyfish. The trigger is Drone-only but the effect is weapon-agnostic once active (you stay blinded even if you switch), matching "用無人機炸掉 jellyfish 就中毒 / 中毒時真方塊變彩虹色 / 不管拿什麼武器都中毒". Alternatives: screen flicker (rejected, no impact); drone-gated decoy cubes (rejected, still weak); poison damaging the player (rejected, there is no player health by design).

### D2 — Sword sweep = a 3-cell row across the view, resolved like the Drone AoE
`BlockEffectSystem.rowTargets(centerBlockId, alongX)` returns the centre block plus the two grid cells at ±1 along one horizontal axis (mirroring `droneAreaTargets`). `PlayerControlState` chooses the axis from the camera facing — sweep along X when the player looks mostly down ±Z, else along Z — so the sweep is always left-to-right across the screen, then routes the SWORD attack through `WeaponSystem.attack` with those targets. Rationale: reuses the integer grid and the existing position→entity mapping; keeps the targeting logic headless-testable. Alternative: a true swept-volume raycast — rejected as overkill for a grid game.

## Risks / Trade-offs

- **Rainbow flicker could be nauseating / too punishing** → Mitigation: the cap is 10s and only the drone triggers it; the palette and 0.12s refresh are constants, easy to soften. Verified by launch.
- **Recolour fights `HitFeedbackState`** (both swap block materials): during poison a hit briefly flashes white then `HitFeedbackState` restores its stored per-type colour, and `PoisonState` re-randomises it ~0.12s later. → Accepted: it's all visual chaos while poisoned, and both restore to the same per-type colour when the poison ends, so nothing is corrupted.
- **Sword sweep + Shell split interaction**: a sword sweep that catches Shells splits each of them → a careless melee into a Shell row also snowballs. → Accepted (consistent with the Shell rule); the player learns not to sweep Shells, same as not droning them.
- **Bar / recolour must not leak into menu/end screens** → Mitigation: `PoisonState` lives in `GameplayState`, restores colours and detaches the bar on `cleanup`/`onDisable`, and only shows the bar while poison > 0.
