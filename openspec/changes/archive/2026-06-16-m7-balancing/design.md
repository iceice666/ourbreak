## Context

All gameplay systems exist but were built with placeholder constants and the GDD's mechanics tables are `TBD`. With one shared weapon base damage, the three weapons play almost identically and the counter-matrix barely registers; the effect magnitudes are arbitrary. M7 is the tuning pass that gives the game its intended *shape*: four rounds that escalate not by raw numbers but by forcing the player to read each defensive wall and rotate weapons accordingly. Durabilities (Sand 1, Coral 2, Shell 1, Rock 4, Jelly 1), the 60 s attack timer, the 1.5-cell Coral range, and the per-round block compositions are fixed by the GDD; M7 tunes the knobs around them.

## Goals / Non-Goals

**Goals:**
- A coherent damage economy where weapon choice is the core skill expression.
- An escalating four-round curve that *teaches* then *tests* the weapon-rotation read.
- Effect magnitudes that make each effect a real consideration, not a number.
- Fill every GDD/TDD `TBD`; retune the affected tests; keep the headless suite green.

**Non-Goals:**
- New mechanics. No Sword 3×1 sweep, no player-death/stun, no per-round block-count ramp.
- Changing GDD-fixed values (durabilities, 60 s timer, 1.5-cell range, compositions, 8 blocks/ring).
- Implementing the Jellyfish flicker *visual* (the duration constant is tuned and reserved for it).

## The design — a four-round weapon-rotation puzzle

The whole game is one idea: **the right weapon trivialises a wall; the wrong weapon punishes you.** Difficulty comes from *composition + effects*, not bigger numbers.

### Damage economy

`damage = base(weapon) × counter(weapon, block)`, with **base** = Sword 1.0 / Gun 2.0 / Drone 1.0 and **counter** = Strong ×2.0 / Weak ×0.5 / Neutral ×1.0. Against the fixed durabilities this gives hits-to-kill:

| | Sand (1) | Coral (2) | Shell (1) | Rock (4) | Jelly (1) |
|---|---|---|---|---|---|
| **Sword** (base 1.0, melee) | **1** | 4 | 2 | 4 | 1 |
| **Gun** (base 2.0, ranged single) | 1 | **1** | 1 | 4 | **1** |
| **Drone** (base 1.0, 3×3 AoE) | 1 | 2 | 2 | **2** | 2 |

Weapon identities fall straight out of this:
- **Sword** — fast, free, melee. King of Sand (1-shot). Helpless against Coral (4 hits *and* slowed) and bad vs Shell (2 hits *and* the reflect lands point-blank). High-risk, close-range.
- **Gun** — precise, safe, single-target. 1-shots almost everything from 20 cells away, so it neutralises *effect* blocks (Coral, Jelly) before they touch you. Its weakness is Rock: 4 shots each, no AoE — attrition.
- **Drone** — the rock-breaker and crowd-clearer (Rock in 2, anything in ≤2, across a 3×3). But it is Weak vs Shell and Jelly, and Jelly disrupts vision *while you pilot it* — so the AoE that saves time can also detonate a row of Shells back into your face.

### The round curve

1. **Round 1 — all Sand.** No effects. Every weapon 1-shots. This is the tutorial: learn movement, aim, the timer. You win however you like.
2. **Round 2 — Sand + Coral.** First lesson. Coral halves your move speed within 1.5 cells and eats 4 Sword swings; the answer is to *back off and shoot* — Gun 1-shots Coral from range. Teaches: range beats slow.
3. **Round 3 — Rock + Shell.** First real tension. Rock is a 4-durability wall that only Drone breaks efficiently (2-shot AoE) — but Drone is Weak vs Shell, and an AoE that clips several Shells triggers the **chain reflect** straight into your health. The read: pick the Shells off first (Gun/Sword, one controlled reflect at a time), *then* Drone the Rock. Teaches: sequence your tools.
4. **Round 4 — Rock + Jelly.** The exam. Rock again wants Drone — but Jelly disrupts your vision exactly when you're piloting it, and Drone is Weak vs Jelly anyway. The read: Gun the Jelly first (1-shot, from range, no vision cost), *then* commit the Drone to the Rock. It combines R2's "range the effect block" with R3's "sequence before you AoE", under the same 60 s clock.

8 blocks per round is one complete ring around the mascot — a single readable wall, not a pile. The escalation is entirely in what that wall is made of.

### Effect tuning

- **Coral slow = 0.5** (move at half speed within 1.5 cells). Strong enough that meleeing past Coral feels like wading; readable enough that the fix (shoot from range) is obvious.
- **Shell reflect = 20**, against **player health = 100**. A single careless Drone AoE through three Shells is 60 — most of your bar — which is the intended "that hurt, sequence it next time" beat. Five reflects empties the bar.
- **Jellyfish flicker = 2.0 s** — the reserved duration for the vision-disrupt filter (M6/visual follow-up); long enough to spoil an aim, short enough to not feel cheap.

## Risks / Trade-offs

- **Shell reflect and player health currently have no consequence** (M5 deliberately decoupled health from win/loss; the GDD defines no player death). As tuned, reflect drains a 100-point bar that does nothing yet — so its teeth are *latent*. → **Recommended immediate follow-up**: a brief **stun / control-loss when health hits 0** (≈2 s of lost attack time), *not* death — this respects the GDD's "win/loss is buildings + timer" while making the reflect read land. M7 deliberately tunes the numbers so that consequence drops in cleanly later; it does not add the mechanic (out of scope).
- **Balance is reasoned, not yet playtested at depth.** The hits-to-kill table is clean and the per-round reads are deliberate, but the exact feel (is R4 beatable in 60 s under vision disruption?) needs play. All values are named constants, so re-tuning is a one-line change with no structural risk. → Mitigation: tests assert *relationships* (Strong > Neutral > Weak; reflect reduces health) and concrete values only where the GDD pins them, so future re-tuning won't thrash the suite.
- **GDD lists a Sword 3×1 sweep and weapon ranges** that aren't implemented. → Out of M7 scope (mechanics, not constants); the damage tuning assumes today's single/area targeting. Flagged for a later mechanics change.
