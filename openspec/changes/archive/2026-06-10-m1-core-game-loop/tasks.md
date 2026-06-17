## 1. ECS Components

- [x] 1.1 Create `PhaseComponent` in `app/src/main/java/com/ourbreak/ecs/components/` — enum field `Phase { BUILD, ATTACK }`, implements `EntityComponent`
- [x] 1.2 Create `RoundComponent` in `ecs/components/` — fields: `int currentRound`, `int maxRounds`, `double remainingSeconds`, implements `EntityComponent`
- [x] 1.3 Create `GameResultComponent` in `ecs/components/` — enum field `Result { IN_PROGRESS, WIN, LOSS }`, implements `EntityComponent`
- [x] 1.4 Create `BlockComponent` stub in `ecs/components/` — no fields, implements `EntityComponent` (M2 will add `blockType`, `durability`, `maxDurability`)

## 2. RoundSystem

- [x] 2.1 Create `RoundSystem extends AbstractGameSystem` skeleton in `app/src/main/java/com/ourbreak/ecs/systems/`; inject `EntityData` via constructor
- [x] 2.2 Implement `initialize()` — create singleton game-state entity with `RoundComponent(1, 4, 60.0)`, `PhaseComponent(BUILD)`, `GameResultComponent(IN_PROGRESS)`; store its `EntityId` as a field
- [x] 2.3 Implement phase transition (e.g. `beginAttackPhase()` public method) — set `PhaseComponent` to ATTACK and `remainingSeconds` to 60.0 on the singleton entity
- [x] 2.4 Implement `update(float tpf)` — if ATTACK phase: decrement `remainingSeconds` by `tpf`, clamp to `Math.max(0, ...)`
- [x] 2.5 In `update()`, when `remainingSeconds <= 0` and result is `IN_PROGRESS`: if `currentRound < maxRounds` → increment `currentRound`, reset to BUILD phase with timer 60.0; else → no-op (VictorySystem handles final-round LOSS)
- [x] 2.6 Guard all state writes in `update()`: skip if `GameResultComponent != IN_PROGRESS`

## 3. VictorySystem

- [x] 3.1 Create `VictorySystem extends AbstractGameSystem` skeleton in `ecs/systems/`; inject `EntityData` and the game-state `EntityId` (or a shared holder) via constructor
- [x] 3.2 Implement `update(float tpf)` — if BUILD phase or result != `IN_PROGRESS`, return early
- [x] 3.3 In `update()`, query block count via `ed.getEntities(BlockComponent.class)`; if count == 0 → write `GameResultComponent(WIN)`
- [x] 3.4 In `update()`, check for final-round loss: if `remainingSeconds == 0` AND `currentRound == maxRounds` AND block count > 0 → write `GameResultComponent(LOSS)`

## 4. Tests

- [x] 4.1 Create `RoundSystemTest` in `app/src/test/java/com/ourbreak//`; set up an in-memory `EntityData` and a `RoundSystem` instance
- [x] 4.2 Test: initial state — `currentRound=1`, `maxRounds=4`, `phase=BUILD`, `result=IN_PROGRESS`, `remainingSeconds=60.0`
- [x] 4.3 Test: `beginAttackPhase()` sets `phase=ATTACK` and `remainingSeconds=60.0`
- [x] 4.4 Test: `update(30.0)` in ATTACK phase decrements `remainingSeconds` to 30.0
- [x] 4.5 Test: timer clamps — `remainingSeconds=10.0`, `update(15.0)` → `remainingSeconds==0.0`
- [x] 4.6 Test: timer does not decrement in BUILD phase
- [x] 4.7 Test: non-final round advancement — `currentRound=1`, timer expires → `currentRound=2`, `phase=BUILD`, result still `IN_PROGRESS`
- [x] 4.8 Test: no overwrite — result already `WIN`, timer expires at `currentRound=4` → result stays `WIN`
- [x] 4.9 Create `VictorySystemTest`; set up `EntityData`, `RoundSystem`, and `VictorySystem` sharing the same `EntityData`
- [x] 4.10 Test: WIN — enter ATTACK phase, create no block entities → `result=WIN`
- [x] 4.11 Test: no WIN in BUILD phase — no block entities present → result unchanged
- [x] 4.12 Test: no WIN when blocks exist — create one block entity during ATTACK → result not `WIN`
- [x] 4.13 Test: LOSS — `currentRound=4=maxRounds`, ATTACK timer expires, block entity present → `result=LOSS`
- [x] 4.14 Test: no LOSS on non-final round — `currentRound=2`, timer expires → result `IN_PROGRESS`
- [x] 4.15 Test: idempotent WIN — result is `WIN`, update again → still `WIN`
- [x] 4.16 Test: idempotent LOSS — result is `LOSS`, update again → still `LOSS`

## 5. Verification

- [x] 5.1 Run `./gradlew test` — zero failures, zero errors
- [x] 5.2 Update `AGENTS.md`: flip M1 to `✅ done`, mark M2 as `⬜ next`
- [ ] 5.3 Commit with `/commit` skill (devlog required — type `feat`)
