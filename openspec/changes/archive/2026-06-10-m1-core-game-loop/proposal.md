## Why

Every subsequent system (blocks, weapons, NPCs) depends on knowing the current round and phase. Implementing the headless game loop now gives a stable, tested foundation that all later milestones can build on without re-litigating win/loss logic.

## What Changes

- Add `RoundComponent` (singleton entity: `currentRound`, `maxRounds`) and `PhaseComponent` (enum `BUILD | ATTACK`)
- Add `RoundSystem` — drives BUILD→ATTACK phase transition, runs a 60 s attack timer (`remainingSeconds`, clamped to 0), advances rounds, and triggers defeat after round 4
- Add `VictorySystem` — during ATTACK phase, immediately wins if all `BlockComponent`-tagged entities are destroyed; triggers loss if round 4 ends with blocks remaining
- Add `RoundSystemTest` and `VictorySystemTest` (headless, JUnit 5) covering the gating scenarios from TDD §10

## Capabilities

### New Capabilities
- `round-system`: Round and phase lifecycle — components (`RoundComponent`, `PhaseComponent`) and `RoundSystem` logic (phase transitions, timer, round advancement, defeat trigger)
- `victory-system`: Win/loss determination — `VictorySystem` checks block-entity count during ATTACK phase for immediate win or end-of-round loss

### Modified Capabilities
*(none — M0 components are unchanged)*

## Impact

- New classes under `app/src/main/java/com/ourbreak/ecs/components/`: `RoundComponent`, `PhaseComponent`
- New classes under `app/src/main/java/com/ourbreak/ecs/systems/`: `RoundSystem`, `VictorySystem`
- New test classes under `app/src/test/java/com/ourbreak/`: `RoundSystemTest`, `VictorySystemTest`
- `VictorySystemTest` uses a `BlockComponent` stub to tag synthetic block entities (stub also needed by M2, so placed in test sources only for now)
- No new Gradle dependencies — pure Java 21 + Zay-ES (already present from M0)
