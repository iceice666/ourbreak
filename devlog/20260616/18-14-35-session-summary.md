# 開發／除錯歷程總整理 — 2026-06-16

> 這份是當天一整段「共同設計 + 除錯 + 打包」過程的中文總結,把我們一起修過、做過的東西整理成一份,方便日後回顧。
> 各項對應的正式 devlog 與 commit 也列在文末。

---

## 一、水母中毒機制(取代原本的幻覺)

- 把舊的「無人機操控時畫面閃爍/幻覺方塊」整個改掉,改成 **中毒(poison)** 系統:
  - **用無人機炸死水母 → 中毒 +5 秒(上限 10 秒)**;用槍/劍清水母**不中毒**。
  - 中毒期間**不管拿什麼武器**,全場真方塊都亂跳**彩虹色**(每 ~0.12 秒換色)→ 分不出方塊種類。
  - 畫面下方中央有一條**會遞減的綠色中毒條** + `POISON` 標籤。
  - 加了**中毒音效**(炸死水母觸發)。
- 新增 `PoisonState`,把 `ModelViewState.colorFor` 開放成 public,並用 geometry user-data 記錄每塊的 base 顏色,讓中毒/打擊閃白能正確還原。

## 二、方塊真實化(seamless 貼圖)

- 五種方塊全部從純色換成你提供的**無縫貼圖**:sand / coral / shell(貝母) / rock / jellyfish。
- `ModelViewState` 改成載入 `Textures/<type>.png`(`Repeat` 平鋪、白色 base 顯示真色),找不到貼圖才 fallback 純色。
- 中毒彩虹、打擊閃白都相容(改用顏色 tint 疊在貼圖上)。

## 三、武器 icon HUD

- HUD 左下角顯示**當前武器 icon**(sword / gun / drone),切武器即時換圖。
- 版面依你的要求:**文字在前、icon 在後**(`Weapon: SWORD [icon]`),保持比例不壓扁。
- icon 背景處理:一開始檔案其實**沒有 alpha**(灰底),我先用 Java flood-fill 自動去背;後來你提供**去好背的 v2/v3**(sword/gun 用 v3 裁切版),換上。
- 武器 readout **放大**(icon 72→110、字級 24→34)。

## 四、操作問題(滑鼠視角 + WASD)

- **免按右鍵的自由視角**:查證後確認根因是 **WSLg/XWayland 封鎖游標 warp**(Wayland 不准程式搬游標),所以標準 `CURSOR_DISABLED` 捕獲式視角在 WSL 整個失效(GLFW issue #2271)。
  - WSLg:改用「游標可見 + 每幀位移差值」轉視角,並加**邊緣繼續轉**解決游標到視窗邊緣會停的問題;左右方向修正過一次。
  - **平台自動切換**:原生 Windows / 桌面 Linux 用 jME 原生捕獲式 FPS 視角,WSL 才用 workaround(`runningUnderWsl()` 偵測)。
- **WASD 移動**:原生 Windows 不能用 WASD(原本交給 FlyByCamera,在捕獲游標模式下沒作用)→ 改成**自己處理移動**(每幀讀 W/A/S/D 沿相機水平方向位移),兩平台一致。
- **出生朝向**:出生時方塊牆在背後 → 改成 `camera.lookAt(原點)` 讓出生**直接面向牆**。

## 五、Windows 打包 + 實際在 Windows 跑起來

- `app/build.gradle.kts` 加上**全平台 LWJGL natives**(windows/linux/macos),讓 WSL 建的 `distZip` 能在原生 Windows 跑;distribution 命名為 `ourbreak`(含 `ourbreak.bat`)。
- 實機啟動排障:
  - 你 Windows 只有 **Java 17**,但遊戲需要 **Java 21** → 下載免安裝的 **Windows JDK 21** 來跑。
  - 解壓被 Windows「解壓全部」**多包了一層**(`ourbreak\ourbreak\`),修正 classpath。
  - 從 WSL **直接執行 Windows 的 java.exe**(互通)啟動,視窗開在桌面、跑在**原生 RTX 5070 OpenGL**(非 WSLg 軟體渲染)。

## 六、沙灘場景

- 地板從暗藍海底色 → **鋪 sand 貼圖**(每 8 單位一塊平鋪);格線改成**淡沙色半透明**;背景改**天空藍**;燈光改**暖陽** → 沙灘陽光感。
- 劍音效音量再調小(0.4 → 0.2)。

## 七、珊瑚重新設計(減速 → 再生)

- 舊的「靠近 1.5 格減速」根本不會觸發(玩家都從遠處拆牆)→ 重新設計成 **再生/蔓延**:
  - 攻擊階段每 **7 秒**,每個活珊瑚在相鄰空洞**長出新珊瑚**(只在攻擊起始的牆範圍內補,封頂=長回整面牆),新珊瑚也會再生(滾雪球但有上限)。
  - 牆會自己癒合 → **只要珊瑚還活著就清不完**,逼你用**槍**優先把珊瑚點掉(斬草除根)。
  - 新增 `CoralGrowthSystem`,核心 `growthTargets` 為純函式 + 單元測試。

## 八、無限難度曲線(取消第 5 round 封頂)

- 原本難度爬到第 5 round 就持平(方塊 48 封頂、時間固定 60s),之後每回合一模一樣。
- 改成以「**要求清除速率 ρ = 方塊數 ÷ 時間**」為主軸的無限曲線:
  - R5+:`ρ(r)=1.20−0.40×0.85^(r−5)`(漸近逼近 1.20、增量逐回合 ×0.85 收斂 → 永不暴衝、永不破表)。
  - 時間 `60+2×(r−5)` 秒緩慢成長(無上限);方塊數 `=round(ρ×時間)` → R5 48 / R10 72 / R20 105 / R30 131…**永遠變大**。
  - 設計理念(數學+心理學):心流通道、Weber–Fechner 不暴衝、像放緩的發散級數無限上升、漸近速率讓「輸是輸在技巧而非遊戲耍賴」。
  - `DifficultyCurveTest` 鎖定:永遠遞增、增量不暴衝、要求速率 < 漸近上限。

---

## 對應 commit

| commit | 內容 |
|--------|------|
| `feat(m5): jellyfish poison, block textures, coral regrowth` | 中毒機制、方塊貼圖、游標/音效、珊瑚再生 |
| `feat(hud): show equipped weapon icon after the name` | 武器 icon HUD |
| `feat(hud): enlarge the weapon readout` | 武器 readout 放大 |
| `feat(controls): free mouse-look with platform auto-switch` | 自由視角 + 平台切換 |
| `build: bundle cross-platform LWJGL natives for a portable distZip` | Windows 打包 |
| `fix(controls): handle WASD movement ourselves so it works on Windows` | WASD 移動修正 |
| `feat(env): sandy beach floor — sand texture, sky, warm light` | 沙灘地板 |
| `fix(controls): face the block wall at spawn` | 出生朝向 |
| `feat(balance): endless difficulty curve, no round-5 plateau` | 無限難度曲線 |
