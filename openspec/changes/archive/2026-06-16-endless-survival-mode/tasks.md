## 1. Round state & timer

- [x] 1.1 Remove `maxRounds` from `RoundComponent` (record + validation); update `RoundComponentTest`
- [x] 1.2 `RoundSystem.initialize` → round 1 / BUILD / IN_PROGRESS / 60 s (no maxRounds)
- [x] 1.3 Remove the timer-expiry auto-advance and the zero-time boundary from `RoundSystem.update` (timer just ticks/clamps)
- [x] 1.4 Add `RoundSystem.advanceRound()` — currentRound + 1, phase BUILD, timer 60 s, only while IN_PROGRESS
- [x] 1.5 Retune `RoundSystemTest` (no max cap, no timeout-advance, new advanceRound)

## 2. Survival / game-over logic

- [x] 2.1 `VictorySystem` (survival): ATTACK + blocks == 0 → `roundSystem.advanceRound()` (no WIN); pass `RoundSystem` into its constructor
- [x] 2.2 `VictorySystem`: ATTACK + remainingSeconds ≤ 0 + blocks > 0 → `GameResultComponent` = LOSS (any round), idempotent
- [x] 2.3 Retune `VictorySystemTest` (advance-on-clear, game-over-on-timeout at any round)

## 3. Escalating NPC walls

- [x] 3.1 `NpcBuilderSystem.blocksForRound(r)` = `min(16 + (r-1)*8, 48)`
- [x] 3.2 `NpcBuilderSystem.blockScript(r)`: rounds 1–4 campaign; round ≥ 5 → ROCK, SHELL, JELLYFISH, CORAL; support any round ≥ 1, fail only round < 1
- [x] 3.3 Retune `NpcBuilderTest` (formula counts, gauntlet from round 5, high-round support)

## 4. HUD & end screen

- [x] 4.1 `HudText.round(current)` → "Round X" (drop max); `HudState` updates; retune `HudTextTest`
- [x] 4.2 `GameEndState` takes the round reached, shows "GAME OVER — Reached Round N" + Restart
- [x] 4.3 `GameplayState`: construct `VictorySystem` with the round system; on LOSS, attach `GameEndState` with `currentRound`

## 5. Design docs

- [x] 5.1 `design/gdd.md`: replace win/loss rules with the survival loop (clear → survive/advance; timeout-with-blocks → game over) and scoring (round reached); describe endless rounds + escalation formulas
- [x] 5.2 `design/tdd.md`: update round/victory/NPC sections to the endless model

## 6. Verification

- [x] 6.1 `./gradlew test` green (retuned round/victory/npc/round-component/hud-text tests)
- [ ] 6.2 Launch: clear a wall → next harder round; HUD shows unbounded round; let a timer expire with blocks → "GAME OVER — Reached Round N"; walls visibly escalate (count + composition)
