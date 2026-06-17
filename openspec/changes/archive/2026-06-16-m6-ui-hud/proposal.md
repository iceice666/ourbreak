## Why

The game is playable but illegible to a first-time player: there is no on-screen round/timer/objective information, and the menu and end screens are placeholder `BitmapText` with key-only controls. M6 adds a real HUD and replaces the placeholder screens with proper Lemur UI so a player can read the game state and click through menus.

## What Changes

- Add **Lemur** (`com.simsilica:lemur:1.16.0`) as a Gradle dependency (approved). Initialize `GuiGlobals` once in `OurbreakGame`; style elements in pure Java (no Groovy / glass-style dependency).
- Add a `HudState` shown during gameplay: round counter (Round X / 4) always visible, and attack countdown + remaining-building count visible only during the ATTACK phase. The display strings are produced by a pure, unit-tested `HudText` helper.
- Replace `MainMenuState`'s placeholder rendering with a Lemur panel of clickable **Start Game** / **Exit** buttons (keyboard shortcuts retained).
- Replace `GameEndState`'s placeholder rendering with a Lemur panel showing the Win/Lose outcome and a clickable **Restart** button.
- Keep the existing screen transitions and gameplay wiring unchanged — only the presentation and the new HUD are added.

## Capabilities

### New Capabilities

- `hud`: Defines the in-gameplay heads-up display — the round counter (always), and the attack countdown and remaining-building count (ATTACK phase only) — and the pure formatting of those values.
- `menu-ui`: Defines the Lemur-rendered main menu (Start Game / Exit) and end screen (Win/Lose + Restart) with clickable controls.

### Modified Capabilities

None. `app-state-machine` screen transitions are unchanged — M6 only changes how the menu/end screens are presented and adds the HUD.

## Impact

- Adds `com.simsilica:lemur:1.16.0` to `gradle/libs.versions.toml` and `app/build.gradle.kts` (transitive deps — jme3-core, guava, slf4j-api — are already present; no Groovy).
- Adds `HudState`, `HudText`, and rewrites `MainMenuState` / `GameEndState` under `ecs/systems/`; `GameplayState` attaches `HudState`; `OurbreakGame` initializes `GuiGlobals`.
- Adds `HudTextTest`; the rest of the HUD/menu work is runtime-visual and verified by launching the app (menu → HUD during a match → end screen).
- The headless suite stays green; no gameplay logic, components, or balancing change.
