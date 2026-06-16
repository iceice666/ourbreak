# 核心玩法概述
遊戲名稱: ourcraft
遊玩時間: 一局約 5 分鐘
遊戲類型: 第一人稱動作遊戲
![[democoncept.png]]

## 角色固定
>考慮到開發時間，目前角色為固定，如時間其實充裕，則玩家可以扮演建造者
- **玩家**：永遠是破壞者
- **NPC**：永遠是建造者

## Round 結構（無限生存模式）
無限個 round，難度逐回合遞增。每個 round 分兩個階段：

| 階段   | 執行者 | 規則                              |
| ---- | --- | ------------------------------- |
| 建造階段 | NPC | 自動在吉祥物周圍放置方塊（牆越來越厚、越來越難），方塊放完結束 |
| 攻擊階段 | 玩家  | 1 分鐘時間限制，要在時間內清光整面牆             |

- 建造階段：玩家無法操作
- 攻擊階段：NPC 靜止不動

## 勝負條件（生存 / 計分）
- **存活並前進**：在攻擊階段時間內清光所有建築 → 視為「撐過這一 round」，進入下一個（更難的）round。**沒有「贏」的終局**。
- **遊戲結束（Game Over）**：任一 round 的攻擊計時器歸零時仍有建築存在 → 立即結束。
- **分數**：撐到第幾個 round（結束畫面顯示 `Reached Round N`）。目標是刷新自己的最高紀錄。

---

# 場景與世界觀

## 世界觀
Clawd 跟 Openclaw 是海灘上的兩個鄰居，兩個都想在沙灘上蓋自己的夢幻沙堡。
有一天 Openclaw 不小心把 Clawd 辛苦蓋的沙堡踩爛了，從此兩個就槓上了，每天在沙灘上互相拆對方的堡壘。
![[ourcraftconcept.png]]

## 場景
- 地點：熱帶沙灘
- 地板：沙地、礁石
- 背景：海浪、椰子樹、晴天

---

# 吉祥物
玩家固定扮演 Openclaw 陣營（破壞者），對手固定為 Clawd（建造者）。
吉祥物本身無 HP，勝負由建築是否存活決定。

| 吉祥物      | 外觀    | 陣營  |
| -------- | ----- | --- |
| Clawd    | 粉紅色章魚 | 閉源派 |
| Openclaw | 紅色龍蝦  | 開源派 |

---

# 方塊（NPC 使用）
NPC 每個建造階段自動放置方塊保護吉祥物。
每 round 可用方塊數量逐回合遞增：`min(16 + (round-1)×8, 48)` → 16 / 24 / 32 / 40 / 48，第 5 round 起固定 48（60 秒可清的上限）。方塊在吉祥物周圍排成**立體同心城牆**：每圈先鋪滿地面層再往上疊高（牆高 3 格），不夠才往外擴一圈。

| 方塊                   | 耐久  | 功能              |
| -------------------- | --- | --------------- |
| Sand Block（沙塊）       | 1 下 | 無特殊，便宜量多，適合快速堆牆 |
| Coral Block（珊瑚塊）     | 2 下 | 玩家進入旁邊範圍時減速     |
| Shell Block（貝殼塊）     | 1 下 | 被打破時反彈傷害給攻擊者    |
| Rock Block（礁石塊）      | 4 下 | 純高耐久，沒有特效       |
| Jellyfish Block（水母塊） | 1 下 | 放置後干擾玩家視野（閃爍效果） |
![[blockconcept.jpg]]

---

# 武器（玩家使用）
玩家在攻擊階段可自由切換武器。
不同武器剋不同方塊，是勝負關鍵。

| 武器         | 攻擊方式          | 剋                                               | 被剋                                            |
| ---------- | ------------- | ----------------------------------------------- | --------------------------------------------- |
| Sword（劍）   | 近戰，一揮可破多個相鄰方塊 | Sand Block（橫掃一排）                                | Shell Block（近身反彈傷害大）、Coral Block（被減速難以近身）     |
| Gun（槍）     | 遠距單體射擊        | Coral Block（遠距不受減速影響）、Jellyfish Block（遠距不受視野干擾） | Rock Block（單體傷害對高耐久效率差）                       |
| Drone（無人機） | 範圍轟炸，需幾秒操控時間  | Rock Block（範圍傷害無視高耐久）、Sand Block（大範圍清除）         | Jellyfish Block（操控時視野被干擾）、Shell Block（引爆連鎖反彈） |
![[weaponconcept.jpg]]
---

# NPC AI
NPC 角色固定為建造者，行為簡單：
- 每個建造階段依照固定模式在吉祥物周圍放置方塊
- 隨著 round 推進，NPC 使用更高耐久或特殊效果的方塊（策略細節 `TBD`）

---

# Production Specifications Document

## 3D model

#### Creature
- Clawd（章魚）
- Openclaw（龍蝦）

#### Blocks
- Sand Block
- Coral Block
- Shell Block
- Rock Block
- Jellyfish Block

#### Weapons
- Sword
- Gun
- Drone

## 2D asset
- button：start game、exit

## Special Effects
- Coral Block：玩家周圍減速粒子效果
- Shell Block：破壞時爆炸反彈效果
- Jellyfish Block：視野閃爍效果
- Drone：爆炸範圍效果

## User Widget

#### main menu
- start game
- select character（Clawd or Openclaw）
>如果開發時間充裕，則可選擇Clawd陣營
![[selectchar.jpg]]
#### game status
- Current Round（第幾 round）
- 剩餘攻擊時間（攻擊階段顯示）
- 剩餘建築數量（攻擊階段顯示）

#### game end
- show win or lose

---

## Mechanics

#### 計時器
| 項目 | 數值 |
| ---- | ---- |
| 攻擊階段時間限制 | 60 秒 |
| 總 round 數 | 無限（生存模式，難度逐回合遞增） |

#### 方塊數值
| 方塊 | 耐久（下） | 特效範圍 / 數值 |
| ---- | --------- | -------------- |
| Sand Block | 1 | — |
| Coral Block | 2 | 減速範圍：1.5 格；移動速度降低 50%（0.5×） |
| Shell Block | 1 | 反彈傷害：20（玩家血量 100） |
| Rock Block | 4 | — |
| Jellyfish Block | 1 | 閃爍持續時間：2 秒 |

每 round NPC 可放方塊數量（逐回合遞增）：`min(16 + (round-1)×8, 48)` → R1 16 / R2 24 / R3 32 / R4 40 / R5+ 48

#### 武器數值
| 武器 | 攻擊距離 | 範圍 | 基礎傷害 |
| ---- | ------- | ---- | ------- |
| Sword | 2 格 | 3x1 橫掃 | 1.0 |
| Gun | 20 格 | 單體 | 2.0 |
| Drone | — | 3x3 爆炸 | 1.0 |

> 實際傷害 = 基礎傷害 × 剋制倍率（剋 ×2.0、被剋 ×0.5、普通 ×1.0）。各武器/方塊的命中次數見 `design/tdd.md`。

#### NPC 建造策略（各 round）
| Round | 方塊組合 |
| ----- | ------- |
| 1 | 全 Sand Block |
| 2 | Sand Block + Coral Block |
| 3 | Rock Block + Shell Block |
| 4 | Rock Block + Jellyfish Block |
| 5+ | Rock + Shell + Jellyfish + Coral（全餐：又肉又多特效） |
