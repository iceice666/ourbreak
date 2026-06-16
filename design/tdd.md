# Technical Design Document — ourcraft

基於 `gdd.md`，描述如何用 JMonkeyEngine 3.9.0 + Zay-ES 1.6.0 實作遊戲。

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
| `MascotComponent` | — | 標記吉祥物 entity（無 HP，勝負由建築決定）|
| `WeaponComponent` | weaponType | 玩家當前持有武器 |
| `PhaseComponent` | phase（BUILD / ATTACK）| 當前回合階段 |
| `RoundComponent` | currentRound, remainingSeconds | 回合狀態（singleton entity，無 max 上限）|
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
| `RoundSystem` | BUILD/ATTACK 切換、1 分鐘計時器、`advanceRound()`（計時器不自動進回合）|
| `VictorySystem` | 生存：攻擊階段方塊清空 → 進下一回合；計時器歸零仍有方塊 → Game Over（任一回合）|
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

**傷害數值（M7 調整，見 `WeaponSystem` 常數）**：實際傷害 = 各武器基礎傷害 × 剋制倍率。基礎傷害 SWORD 1.0 / GUN 2.0 / DRONE 1.0；倍率 剋 ×2.0、被剋 ×0.5、普通 ×1.0。各組合的命中次數（耐久 ÷ 傷害）：

| | SAND(1) | CORAL(2) | SHELL(1) | ROCK(4) | JELLY(1) |
|---|---|---|---|---|---|
| SWORD | 1 | 4 | 2 | 4 | 1 |
| GUN | 1 | 1 | 1 | 4 | 1 |
| DRONE | 1 | 2 | 2 | 2 | 2 |

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
| 5+ | ROCK + SHELL + JELLYFISH + CORAL（全餐）|

每 round 方塊數 = `min(16 + (round-1)×8, 48)`。`NpcBuilderSystem` 在 BUILD phase 放完方塊後觸發切換至 ATTACK phase。

---

## 8. Round 管理

無限生存模式：

```
遊戲開始（GameplayState attach）
  └─ Round N（N 從 1 起，無上限）
       ├─ BUILD phase
       │    NpcBuilderSystem 依公式放方塊（數量/組成隨 N 遞增）
       │    方塊放完 → 切換 ATTACK phase
       └─ ATTACK phase
            玩家 1 分鐘攻擊
            清光所有方塊 → 存活 → RoundSystem.advanceRound() → Round N+1（更難）
            計時器歸零仍有方塊 → Game Over（LOSS）
  └─ 重複，直到 Game Over
遊戲結束 → GameEndState（顯示 Reached Round N）
```

`RoundSystem` 持有計時器（remainingSeconds），每幀 `update(tpf)` 倒數但**不再自動進回合**；
回合推進改由 `VictorySystem`（生存）在方塊清空時呼叫 `advanceRound()`，逾時仍有方塊則寫入 LOSS。

---

## 9. UI（Lemur GUI）

### HUD（GameplayState 期間常駐）

| 位置 | 元素 |
|------|------|
| 左上 | 回合數（Round X，無上限）|
| 右上 | 剩餘時間倒數（ATTACK phase 顯示）|
| 中上 | 剩餘建築數量（ATTACK phase 顯示）|

### 其他畫面

| 畫面 | 元素 |
|------|------|
| MainMenuState | Start Game、Exit |
| MainMenuState | Start Game、Exit（玩家固定為 Openclaw）|
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
| `VictorySystemTest` | 方塊清空 → 進下一回合（存活）、計時器歸零仍有方塊 → Game Over（任一回合）|
| `NpcBuilderTest` | 各 round 放置正確方塊種類 |
