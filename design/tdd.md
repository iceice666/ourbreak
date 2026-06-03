# TDD Plan — ourcraft

基於 `gameplay.md`，以下為各核心模組的測試案例。
測試框架：JUnit 5，套件根：`com.ourcraft`。

---

## 1. 武器（Weapon）

### WeaponTest

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 1 | `axe_hasDamageThree` | 建立斧頭 | `damage == 3` |
| 3 | `tnt_hasDamageTen` | 建立炸藥 | `damage == 10` |
| 4 | `scoutDrone_hasDamageTwo` | 建立偵察無人機 | `damage == 2` |
| 5 | `heavyDrone_hasDamageEight` | 建立重型無人機 | `damage == 8` |
| 6 | `tnt_hasCooldown` | 炸藥冷卻 | `cooldown > 0` |
| 8 | `tnt_affectsThreeByThreeArea` | 炸藥爆炸範圍 | 回傳 9 個受影響格子 |
| 9 | `scoutDrone_expiresAfterLifetime` | 偵察無人機存活時間到 | `isExpired == true` |
| 10 | `heavyDrone_expiresAfterLifetime` | 重型無人機存活時間到 | `isExpired == true` |

---

## 2. 方塊（Block）

### BlockTest

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 1 | `woodBlock_hasDurabilityTen` | 建立木頭方塊 | `durability == 10` |
| 2 | `stoneBlock_hasDurabilityTwentyFive` | 建立石頭方塊 | `durability == 25` |
| 3 | `steelBlock_hasDurabilityFifty` | 建立鋼鐵方塊 | `durability == 50` |
| 4 | `coreBlock_hasDurabilityOneHundred` | 建立核心方塊 | `durability == 100` |
| 5 | `block_isDestroyedWhenDurabilityReachesZero` | 堅固值降到 0 | `isDestroyed == true` |
| 6 | `block_durabilityCannotGoBelowZero` | 超額傷害 | `durability == 0`，不為負數 |
| 7 | `block_canBeRepaired` | NPC 修補方塊 | `durability` 增加 |
| 8 | `block_durabilityCannotExceedMax` | 修補超過上限 | `durability == maxDurability` |
| 9 | `coreBlock_cannotBeRepaired` | NPC 嘗試修補核心方塊 | 堅固值不變（核心不可修補）|

---

## 3. 戰鬥系統（Combat）

### CombatSystemTest

**基本公式**

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 1 | `attack_reducesDurabilityByWeaponDamage` | 斧頭（3）攻擊木頭方塊（10）| `durability == 7` |
| 2 | `attack_clampsDurabilityToZero` | 斧頭（3）攻擊堅固值 2 的方塊 | `durability == 0`，不為負數 |
| 3 | `attack_destroysBlock_whenDurabilityReachesZero` | 堅固值扣到 0 | `isDestroyed == true` |

**各武器對各方塊所需攻擊次數**

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 4 | `axe_destroysWoodBlock_inFourHits` | 斧頭連續攻擊木頭方塊 4 次 | 方塊被摧毀 |
| 5 | `axe_doesNotDestroyStoneBlock_inFourHits` | 斧頭攻擊石頭方塊 4 次 | 方塊仍存活（剩餘 durability == 13）|
| 6 | `tnt_destroysWoodBlock_inOneHit` | 炸藥攻擊木頭方塊 1 次 | 方塊被摧毀 |
| 7 | `tnt_doesNotDestroySteelBlock_inOneHit` | 炸藥攻擊鋼鐵方塊 1 次 | 方塊仍存活（durability == 40）|

**炸藥範圍**

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 8 | `tnt_damagesAllBlocksInThreeByThreeArea` | 3x3 範圍內有 9 個方塊 | 9 個方塊各扣 10 破壞值 |
| 9 | `tnt_doesNotDamageBlocksOutsideArea` | 範圍外有方塊 | 範圍外方塊堅固值不變 |

**無人機**

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 10 | `scoutDrone_attacksNearestBlock` | 偵察無人機，附近有多個方塊 | 攻擊最近的方塊 |
| 11 | `heavyDrone_focusesSingleTarget` | 重型無人機，附近有多個方塊 | 持續攻擊同一目標，不切換 |
| 12 | `drone_dealsCorrectDamagePerHit` | 重型無人機（8）攻擊石頭方塊（25）一次 | `durability == 17` |

---

## 4. NPC 行為（NPC AI）

### NpcTest

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 1 | `npc_placesBlockOnEmptyTile` | NPC 回合，空格存在 | 空格上出現新方塊 |
| 2 | `npc_repairsDestroyedBlock` | 方塊被破壞 | NPC 優先修補該方塊 |
| 3 | `npc_repairIncreasesBlockDurability` | NPC 修補動作執行 | 方塊堅固值上升 |

---

## 5. 勝利條件（Victory Condition）

### VictoryConditionTest

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 1 | `playerWins_whenCoreBlockDestroyed` | 核心方塊堅固值降到 0 | 回傳 `PLAYER_WIN` |
| 2 | `npcWins_whenTimerExpires_andCoreBlockSurvives` | 時間到，核心方塊仍存活 | 回傳 `NPC_WIN` |
| 3 | `gameNotOver_whenTimerRunning_andCoreBlockSurvives` | 時間未到，核心方塊存活 | 回傳 `IN_PROGRESS` |
| 4 | `playerWins_immediately_whenCoreBlockDestroyed` | 核心方塊被摧毀 | 立即回傳 `PLAYER_WIN`，不等時間到 |

---

## 6. 遊戲計時器（GameTimer）

### GameTimerTest

| # | 測試名稱 | 情境 | 預期結果 |
|---|----------|------|----------|
| 1 | `timer_startsAtFourMinutes` | 初始化計時器 | `remaining == 240` 秒 |
| 2 | `timer_decreasesOverTime` | 推進時間 1 秒 | `remaining == 239` |
| 3 | `timer_isExpiredWhenReachesZero` | 計時器到 0 | `isExpired == true` |
| 4 | `timer_doesNotGoBelowZero` | 繼續推進已到期的計時器 | `remaining == 0` |
