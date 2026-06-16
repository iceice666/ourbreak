## Context

Today the match is fixed at 4 rounds: `RoundComponent(currentRound, maxRounds=4, remainingSeconds)`; `RoundSystem` advances the round when the 60 s attack timer expires (until round 4); `VictorySystem` declares WIN the instant all blocks are cleared and LOSS if round 4's timer expires with blocks remaining; `NpcBuilderSystem` hardcodes a 1–4 composition table and a per-round count. The result: you "win" by clearing one wall, and there is no reason to replay. Endless mode reuses all of this but flips the meta-loop into survival.

## Goals / Non-Goals

**Goals:**
- Infinite rounds with difficulty that escalates round over round (count + composition).
- Clear-the-wall = survive the round and advance; timer-out-with-blocks = game over at any round.
- Score = how far you got (round reached); end screen and HUD reflect this.
- All existing combat (weapons, effects, M7 tuning, juice) unchanged.

**Non-Goals:**
- Persistent high-score storage / leaderboards (show this run's result only).
- A separate "campaign vs endless" mode select — endless becomes *the* mode.
- New block types or mechanics; only the round meta-loop and scaling change.

## Decisions

### D1 — Drop the round cap; advance on *clear*, fail on *timeout*
`RoundComponent` loses `maxRounds` (it only encoded the 4-round cap). The round lifecycle becomes:
- **BUILD**: NPC places the round's wall, then transitions to ATTACK (unchanged trigger).
- **ATTACK**: the 60 s timer ticks down (clamped at 0). `RoundSystem` no longer auto-advances the round on timeout.
- **Survived** — all blocks cleared during ATTACK → advance to round N+1 (BUILD), reset the timer. This replaces the old WIN.
- **Game over** — the timer reaches 0 with blocks still standing → result LOSS, run ends. This replaces the old round-4 LOSS and now applies to *any* round.

Rationale: the only two outcomes of an attack phase are "cleared in time → harder round" or "ran out of time → dead", which is the whole survival loop. Alternative (keep timeout-advance, add a separate death rule) was rejected as muddled.

### D2 — Survival/progression lives where block-count is known
The "all blocks cleared" and "timer out with blocks remaining" checks both need the live block count, which `VictorySystem` already owns (its `BlockComponent` `EntitySet`). So `VictorySystem` becomes the survival/progression system:
- ATTACK + blocks == 0 → call `RoundSystem.advanceRound()` (round+1, phase BUILD, timer reset). No win state is set.
- ATTACK + remainingSeconds ≤ 0 + blocks > 0 → set `GameResultComponent` to LOSS (game over).
`RoundSystem` keeps timer ticking + `beginAttackPhase()` (called by the NPC builder when the wall is complete) + the new `advanceRound()`; it drops the maxRounds/auto-advance logic. Rationale: keeps the block-count authority in one place and makes `RoundSystem` a pure timer/phase machine.

### D3 — Formula-driven escalation in the NPC builder
Replace the hardcoded 1–4 tables with functions of the round number `r`:
- **Block count** `blocksForRound(r) = min(16 + (r-1)*8, 48)` → 16, 24, 32, 40, 48, then plateau at 48 (a 48-block wall is already near the ceiling of what 60 s allows; beyond that it would be unwinnable). The first rounds keep the M7-tuned ramp.
- **Composition** `blockScript(r)`:
  - r1 `[SAND]`, r2 `[SAND, CORAL]`, r3 `[ROCK, SHELL]`, r4 `[ROCK, JELLYFISH]` — the tuned campaign opening doubles as the endless on-ramp.
  - r ≥ 5 `[ROCK, SHELL, JELLYFISH, CORAL]` — the full gauntlet (tanky + every effect) from then on.
Rationale: escalation over the first ~5 rounds via count *and* nastiness, then a sustained max wall — survival becomes "how many max walls can you chain". Both the cap (48) and the gauntlet are named constants, easy to retune. A continued-escalation knob (mild timer reduction or a durability multiplier past the cap) is noted as a future tunable if the plateau feels flat.

### D4 — Score = round reached; UI reflects survival
- `HudText.round` becomes `round(current)` → `"Round X"` (no max); `HudState` updates accordingly.
- `GameEndState` takes the round reached instead of a WIN/LOSS result and shows `GAME OVER — Reached Round N` + Restart. `GameplayState` passes `currentRound` when it observes LOSS.
- `GameResultComponent.Result` keeps `IN_PROGRESS` and `LOSS`; `WIN` becomes unused (endless has no win). Removing `WIN` entirely is a larger ripple (component test, enum users) — keep the enum, document `WIN` as vestigial, and revisit if we want it gone.

## Risks / Trade-offs

- **Difficulty plateaus at the count cap (round 5+).** → Mitigation: the gauntlet composition + 48-block wall is already a hard sustained challenge; if it reads as "stops getting harder", turn on the noted timer-reduction / durability-multiplier knob. Designed in as a constant, not a rewrite.
- **Large test ripple** (round-system, victory-system, npc-building, round-component, hud-text tests all change). → Mitigation: the changes are mechanical (cap removal, advance-on-clear, formula counts); tests assert the *rules* (advance on clear, game-over on timeout, escalating counts) so they stay meaningful. This is expected scope for a meta-loop change.
- **`RoundComponent` losing `maxRounds` touches everything that reads it.** → Mitigation: it's only read by HUD (now shows current only) and the round/victory logic being rewritten anyway; a focused, compiler-guided refactor.
- **No "you win" payoff.** → Accepted by design: the payoff is beating your previous round count. (A persistent best-score is an easy future add.)
