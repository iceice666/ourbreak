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
| `BlockEffectSystem` | Coral 減速、Drone 3×3、Sword 3-cell 橫排目標 |
| `CoralGrowthSystem` | 攻擊階段每 7 秒讓每個活珊瑚在相鄰空洞長回新珊瑚（限攻擊起始的牆 footprint 內）|
| `PoisonState` | 無人機炸死 Jellyfish → 中毒計時（+5s/上限 10s）、中毒時真方塊彩虹化、中毒條 UI |
| `NpcBuilderSystem` | BUILD phase：腳本放方塊保護吉祥物 |
| `RoundSystem` | BUILD/ATTACK 切換、1 分鐘計時器、`advanceRound()`（計時器不自動進回合）|
| `VictorySystem` | 生存：攻擊階段方塊清空 → 進下一回合；計時器歸零仍有方塊 → Game Over（任一回合）|
| `HudState` | 更新 HUD 顯示（回合 / 倒數 / 剩餘建築 / 武器）|

---

## 4. 第一人稱控制

| 操作 | 輸入 |
|------|------|
| 移動 | WASD |
| 視角 | 滑鼠直接轉視角（免按鍵）。平台自動切換：原生 Windows/桌面 Linux 用 jME 捕獲式 FPS 視角；WSLg 因不能 warp 游標，改用「游標可見 + 每幀位移差值 + 邊緣繼續轉」workaround |
| 攻擊 | 左鍵 |
| 武器切換 | 1 / 2 / 3 鍵，或 Q 循環切換 |

> WSLg/XWayland 封鎖 `glfwSetCursorPos`/cursor warp，標準 `CURSOR_DISABLED` 捕獲式視角在 WSL 下整個失效（GLFW issue #2271）。`PlayerControlState.runningUnderWsl()` 用 `os.name` + `WSL_DISTRO_NAME`/`WSL_INTEROP`/`/proc/version` 偵測,只在 WSL 走 workaround。

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
| CORAL | 2 | `CoralGrowthSystem`：ATTACK 第一幀快照整面牆當 footprint；每 7 秒每個活珊瑚在相鄰（6 面）、footprint 內、空的格子長一塊新珊瑚（純函式 `growthTargets` 決定目標，reserved 防同格重複），新珊瑚也會再生但被 footprint 封頂。次要：`BlockEffectSystem` 偵測玩家在 1.5 格內 → 移動 ×0.5 |
| SHELL | 1 | `WeaponSystem`：被劍/無人機打 → 分裂成 2 個新貝殼（無上限）；槍乾淨清掉 |
| ROCK | 4 | 無 |
| JELLYFISH | 1 | `PoisonState`：偵測「被無人機炸死的 Jellyfish」→ 中毒 +5s（上限 10s）；中毒時把所有真方塊 geometry 重新上隨機彩虹色（每 ~0.12s 換色，與武器無關），歸零時用 `ModelViewState.colorFor` 還原；GUI 畫遞減中毒條。槍/劍清掉不中毒 |

---

## 6. 武器系統

| 武器 | 攻擊方式 | 實作 |
|------|----------|------|
| SWORD | 近戰橫掃 | 準星 raycast 取中心方塊，再經 `BlockEffectSystem.rowTargets` 沿視角橫向 ±1 格展成 3 格，每格各算一次命中 |
| GUN | 遠距單體 | 長距 raycast（20 格），命中第一個方塊 |
| DRONE | 範圍轟炸 | 對準星方塊為中心，立即對 `(2·Lv+1)²` 方陣造成傷害（`droneAreaTargets(center, radius)`）。**等級隨難度升級**：`WeaponSystem.droneLevelForRound(round) = 1+(round−1)/3`（每 3 回合 +1、無上限）→ 3×3→5×5→7×7…，爆炸特效與 HUD `Lv.N` 同步。無獨立俯視操控模式 |

### 武器剋制關係（影響 `WeaponSystem` 傷害倍數）

| 武器 | 剋 | 被剋 |
|------|----|------|
| Sword | SAND（橫掃清排）| SHELL（揮到會分裂雪崩）、CORAL（牆一直長回來、近身又被減速）|
| Gun | CORAL（一發點掉再生源、斬草除根）、JELLYFISH（清掉不中毒）| ROCK（單體低效）|
| Drone | ROCK（範圍破高耐久）、SAND（大範圍清除）| JELLYFISH（炸死會中毒，全場彩虹分不出方塊）、SHELL（引爆連鎖反彈）|

**傷害數值（見 `WeaponSystem` 常數）**：實際傷害 = 各武器基礎傷害 × 剋制倍率。基礎傷害 SWORD 1.0 / GUN 8.0 / DRONE 1.0；倍率 剋 ×2.0、被剋 ×0.5、普通 ×1.0。槍基礎傷害極高 → 單體一槍秒殺任何方塊（賣點是單體爆發，弱點是沒有 AoE）。**貝殼不走傷害模型**，改由分裂機制處理。各組合命中次數（耐久 ÷ 傷害）：

| | SAND(1) | CORAL(2) | SHELL(1) | ROCK(4) | JELLY(1) |
|---|---|---|---|---|---|
| SWORD | 1 | 4 | 分裂 | 4 | 1 |
| GUN | 1 | 1 | 1（乾淨清） | 1 | 1 |
| DRONE | 1 | 2 | 分裂 | 2 | 2 |

> SHELL 欄：貝殼不走傷害模型——被 SWORD/DRONE 打會**分裂成 2 個新貝殼（無上限）**，只有 GUN 能一發乾淨清掉。

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

每 round 方塊數：R1–4 = `16+(r-1)×8`；R5+（無上限）= `round(ρ(r)×RoundSystem.attackSecondsForRound(r))`,其中 `ρ(r)=RATE_MAX−(RATE_MAX−RATE_BASE)×RATE_DECAY^(r−5)`（0.80→漸近 1.20,decay 0.85）。攻擊時間 `attackSecondsForRound(r)`：R≤5 = 60,之後 `60+2×(r−5)`（無上限）。`NpcBuilderSystem` 在 BUILD phase 放完方塊後觸發切換至 ATTACK phase。難度曲線由 `DifficultyCurveTest` 鎖定（永遠遞增、增量不暴衝、要求速率 < 漸近上限）。

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
| 左下 | 當前武器名稱 |
| 下方中央 | 中毒條（`PoisonState`，僅中毒時顯示）|

### 其他畫面

| 畫面 | 元素 |
|------|------|
| MainMenuState | Start Game、Exit（玩家固定為 Openclaw）|
| GameEndState | `GAME OVER` + `Reached Round N`、Restart（無 Win 終局）|

---

## 10. 測試策略

所有遊戲邏輯用 **headless unit test**（不開視窗，不依賴 JME render thread）。

| 測試類別 | 覆蓋範圍 |
|----------|----------|
| `BlockTest` | 5 種方塊耐久值、超額傷害 clamp 到 0 |
| `WeaponTest` | 3 種武器命中傷害計算、剋制倍數 |
| `BlockEffectTest` | Coral 減速觸發條件、Drone 3×3 與 Sword 3-cell 橫排目標展開（滿排／稀疏／孤立）|
| `CoralGrowthTest` | `growthTargets` 純函式：補相鄰空洞、被包住不長、不長出 footprint 外、兩珊瑚不搶同格 |
| `RoundSystemTest` | BUILD→ATTACK 切換、計時器倒數、time clamp 到 0 |
| `VictorySystemTest` | 方塊清空 → 進下一回合（存活）、計時器歸零仍有方塊 → Game Over（任一回合）|
| `NpcBuilderTest` | 各 round 放置正確方塊種類 |
| `DifficultyCurveTest` | 無限難度曲線：教學斜坡 + R5 接續 48、方塊永遠遞增、增量不暴衝、要求速率 < 漸近上限 |
| `DroneLevelTest` | 無人機等級每 3 回合 +1、無上限、單調遞增；`droneAreaTargets` 半徑展開（5×5/3×3）|
