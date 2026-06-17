## 1. Dependency & Lemur bootstrap

- [x] 1.1 Add `com.simsilica:lemur:1.16.0` to `gradle/libs.versions.toml` and `app/build.gradle.kts`
- [x] 1.2 Initialize `GuiGlobals.initialize(this)` in `OurcraftGame.simpleInitApp` (no glass/Groovy style)

## 2. HUD

- [x] 2.1 Add a pure `HudText` helper: round text, countdown text (M:SS, seconds rounded up, clamped ≥ 0), building-count text
- [x] 2.2 Add `HudState` (`BaseAppState`) holding Lemur `Label`s; read `RoundComponent`/`PhaseComponent` and count `BlockComponent` entities each frame; set label text via `HudText`
- [x] 2.3 Place round counter top-left (always), attack countdown top-right and building count top-centre (ATTACK phase only — hide otherwise)
- [x] 2.4 In `GameplayState`, attach `HudState` during world construction and detach it (releasing its `EntitySet`) on cleanup
- [x] 2.5 Show the current weapon (bottom-left, both phases) and add a Q key in `PlayerControlState` to cycle weapons (1/2/3 still select directly)

## 3. Lemur menus

- [x] 3.1 Rewrite `MainMenuState` with a Lemur container: title + clickable Start Game / Exit buttons wired to the existing transitions; keep Enter/Esc shortcuts; remove the placeholder `BitmapText`
- [x] 3.2 Rewrite `GameEndState` with a Lemur panel: Win/Lose label + clickable Restart button wired to the existing transition; keep the Enter shortcut
- [x] 3.3 Ensure each screen attaches its UI on enable and removes it on disable (no leftover GUI)

## 4. Tests & verification

- [x] 4.1 Add `HudTextTest` covering round text, countdown (`1:00`, round-up `0:01`, clamp `0:00`), and building-count text
- [x] 4.2 Run `./gradlew test`; the full headless suite incl. `HudTextTest` is green
- [x] 4.3 Launch the app and walk through: menu buttons click → HUD shows round/countdown/buildings during a match (countdown+buildings only in ATTACK) → end screen Win/Lose + Restart returns to menu
