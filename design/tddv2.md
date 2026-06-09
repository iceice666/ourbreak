# Technical Design Document — ourcraft

基於 `gddv2.md`，描述如何用 JMonkeyEngine 3.9.0 + Zay-ES 1.6.0 實作遊戲。

---

## 1. 技術堆疊

| 項目 | 版本 |
|------|------|
| Java | 21 |
| JMonkeyEngine | 3.9.0-stable（jme3-core, desktop, lwjgl3, plugins, effects）|
| Zay-ES | 1.6.0 |
| UI | Lemur GUI |
| 測試 | JUnit 5 |

---

## 2. AppState 狀態機

用 JME 的 `AppState` 管理畫面切換，切換時 attach/detach：

```
MainMenuState
  └─→ CharacterSelectState
        └─→ GameplayState
              └─→ GameEndState
                    └─→ MainMenuState（重玩）
```

每個 State 自行管理 UI 與輸入綁定，互不干涉。

---

## 3. ECS 設計（Zay-ES）

### 3.1 Components

| Component | 欄位 | 說明 |
|-----------|------|------|
| `PositionComponent` | x, y, z | 已有，世界座標（整數格） |
| `ModelComponent` | modelId | 已有，對應 3D 模型 ID |
| `BlockComponent` | blockType, durability, maxDurability | 方塊類型與血量 |
| `MascotComponent` | hp, maxHp | 吉祥物生命值 |
| `WeaponComponent` | weaponType | 玩家當前持有武器 |
| `PhaseComponent` | phase（BUILD / ATTACK）| 當前回合階段 |
| `RoundComponent` | currentRound, maxRounds | 回合狀態（singleton entity）|
| `EffectComponent` | effectType | 方塊特效標記 |
| `PlayerComponent` | — | 標記玩家 entity |

### 3.2 Systems（AppState）

| System | 職責 |
|--------|------|
| `ModelViewState` | 已有，渲染有 Position + Model 的 entity |
| `PlayerControlState` | WASD 移動、滑鼠視角、武器切換輸入 |
| `WeaponSystem` | 攻擊輸入 → raycast 命中判定 → 扣血 |
| `BlockEffectSystem` | Coral 減速、Shell 反彈、Jellyfish 閃爍 |
| `NpcBuilderSystem` | BUILD phase：腳本放方塊保護吉祥物 |
| `RoundSystem` | BUILD/ATTACK 切換、1 分鐘計時器、phase 推進 |
| `VictorySystem` | 吉祥物 HP=0 → 勝；4 rounds 結束 → 敗 |
| `HudSystem` | 更新 HUD 顯示 |

---

## 4. 第一人稱控制

| 操作 | 輸入 |
|------|------|
| 移動 | WASD |
| 視角 | 滑鼠（捕捉模式）|
| 攻擊 | 左鍵 |
| 武器切換 | 1 / 2 / 3 鍵 |

`PlayerControlState` 透過 JME `InputManager` 綁定，讀取輸入後更新 `PositionComponent` 或觸發 `WeaponSystem`。

---

## 5. 方塊系統

- 方塊放置在整數格座標（grid）上
- 每個方塊 = 一個 Zay-ES Entity，帶有：
  `PositionComponent + BlockComponent + ModelComponent`
- `BlockComponent.blockType` 決定外觀（modelId）與特效

### 方塊規格

| 方塊 | 耐久 | 特效實作 |
|------|------|----------|
| SAND | 1 | 無 |
| CORAL | 2 | `BlockEffectSystem` 偵測玩家在 1.5 格內 → 降低移動速度 |
| SHELL | 1 | `BlockEffectSystem` 監聽 block destroy → 對玩家造成反彈傷害 |
| ROCK | 4 | 無 |
| JELLYFISH | 1 | `BlockEffectSystem` 在玩家 HUD 套上閃爍 post-process filter |

---

## 6. 武器系統

| 武器 | 攻擊方式 | 實作 |
|------|----------|------|
| SWORD | 近戰 AoE | 短距 raycast（2 格）+ 左右各 1 格橫掃，每格各算一次命中 |
| GUN | 遠距單體 | 長距 raycast（20 格），命中第一個方塊 |
| DRONE | 範圍轟炸 | 切換 drone 控制模式（俯視相機），確認目標後爆炸傷害 3x3 範圍 |

### 武器剋制關係（影響 `WeaponSystem` 傷害倍數）

| 武器 | 剋 | 被剋 |
|------|----|------|
| Sword | SAND（橫掃清排）| SHELL（近身反彈）、CORAL（減速難近身）|
| Gun | CORAL（遠距免疫減速）、JELLYFISH（遠距免疫閃爍）| ROCK（單體低效）|
| Drone | ROCK（範圍破高耐久）、SAND（大範圍清除）| JELLYFISH（操控時閃爍）、SHELL（引爆連鎖反彈）|

---

## 7. NPC AI（建造者腳本）

NPC 為純固定腳本，不使用 pathfinding。

**建造優先順序：** 吉祥物正前方 → 左右兩側 → 外圈

**各 round 放置方塊策略：**

| Round | 方塊組合 |
|-------|----------|
| 1 | 全 SAND |
| 2 | SAND + CORAL |
| 3 | ROCK + SHELL |
| 4 | ROCK + JELLYFISH |

`NpcBuilderSystem` 在 BUILD phase 被 attach，方塊放完後 detach，觸發切換至 ATTACK phase。

---

## 8. Round 管理

```
遊戲開始（GameplayState attach）
  └─ Round 1
       ├─ BUILD phase
       │    NpcBuilderSystem 執行腳本放方塊
       │    方塊用完 → 切換 ATTACK phase
       └─ ATTACK phase
            玩家 1 分鐘攻擊
            時間到 OR 吉祥物死亡 → 進入下一 round / 結束
  └─ Round 2 → Round 3 → Round 4
       （重複）
遊戲結束 → GameEndState
```

`RoundSystem` 持有一個計時器（float remainingSeconds），每幀 `update(tpf)` 倒數。

---

## 9. UI（Lemur GUI）

### HUD（GameplayState 期間常駐）

| 位置 | 元素 |
|------|------|
| 左上 | 回合數（Round X / 4）|
| 右上 | 剩餘時間倒數（ATTACK phase 顯示）|
| 中上 | 吉祥物 HP 條 |

### 其他畫面

| 畫面 | 元素 |
|------|------|
| MainMenuState | Start Game、Exit |
| CharacterSelectState | 選 Clawd 或 Openclaw |
| GameEndState | Win / Lose 文字、Restart |

---

## 10. 測試策略

所有遊戲邏輯用 **headless unit test**（不開視窗，不依賴 JME render thread）。

| 測試類別 | 覆蓋範圍 |
|----------|----------|
| `BlockTest` | 5 種方塊耐久值、超額傷害 clamp 到 0 |
| `WeaponTest` | 3 種武器命中傷害計算、剋制倍數 |
| `BlockEffectTest` | Coral 減速觸發條件、Shell 反彈條件、Jellyfish 觸發條件 |
| `RoundSystemTest` | BUILD→ATTACK 切換、計時器倒數、time clamp 到 0 |
| `VictorySystemTest` | 吉祥物 HP=0 → PLAYER_WIN、4 rounds 結束 → PLAYER_LOSE |
| `NpcBuilderTest` | 各 round 放置正確方塊種類 |
