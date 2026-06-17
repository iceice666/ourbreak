## Context

M4 stood up the AppState machine with placeholder `BitmapText` menus and no HUD; M5 added effects. The GDD (§game status) calls for an in-game HUD — current round, remaining attack time, remaining building count — and proper Start/Exit and Win/Lose+Restart screens. M6 delivers these with Lemur (`com.simsilica:lemur`), the jME GUI toolkit from the same author as the Zay-ES we already use. Lemur 1.16.0's only dependencies (jme3-core, guava, slf4j-api) are already on the classpath, so the footprint is just Lemur itself. The UI is runtime-visual, so — like the other AppStates — it is verified by launching the app (now possible in this WSLg environment), with the one piece of pure logic (display formatting) unit-tested.

## Goals / Non-Goals

**Goals:**
- A gameplay HUD: round counter always; attack countdown and remaining-building count during ATTACK only.
- Lemur main menu (Start Game / Exit) and end screen (Win/Lose + Restart) with clickable buttons.
- Pure, tested formatting of HUD values; headless suite stays green.

**Non-Goals:**
- Character/faction select, settings screens, volume controls (not in M6 scope).
- The Groovy-based "glass" style — styling is done in plain Java.
- Any gameplay/component/balancing change; HUD is read-only over existing ECS state.
- Replacing the in-world crosshair (it stays in `PlayerControlState`).

## Decisions

### D1 — Lemur with `GuiGlobals` initialized once, pure-Java styling
`OurbreakGame.simpleInitApp` calls `GuiGlobals.initialize(this)` before any state is attached. We do **not** call `BaseStyles.loadGlassStyle()` (that needs Groovy); elements use Lemur's built-in default style, set/tweaked via the Java `Styles`/attribute API where needed. Rationale: clickable, readable widgets with zero Groovy footprint. Alternative: load the glass style — rejected (pulls Groovy for cosmetics we don't need yet).

### D2 — `HudState` reads ECS, `HudText` formats (testable seam)
`HudState` (a `BaseAppState` attached by `GameplayState`) holds Lemur `Label`s on the GUI node. Each frame it reads `RoundComponent` + `PhaseComponent` from the game-state entity and counts `BlockComponent` entities via an `EntitySet` (released on cleanup), then sets label text from a pure `HudText` helper and toggles the ATTACK-only labels' visibility. Rationale: all string/threshold logic lives in `HudText` (no jME), so it is unit-tested; the state is a thin view. Alternative: format inline in the state — rejected (untestable).

### D3 — HUD content and phase gating (GDD §game status)
- Round counter `Round X / 4`: visible in both BUILD and ATTACK.
- Attack countdown `M:SS` (from `RoundComponent.remainingSeconds`, rounded up, clamped ≥ 0): visible only in ATTACK.
- Remaining buildings `Buildings: N`: visible only in ATTACK.
Rationale: matches the GDD's status panel exactly. Placement: round top-left, countdown top-right, buildings top-centre (per `design/milestones.md`).

### D4 — Menus rebuilt with Lemur, transitions unchanged
`MainMenuState`/`GameEndState` build a Lemur `Container` with `Button`s whose click commands invoke the same detach/attach transitions they already perform; existing key shortcuts (Enter/Esc) are kept as accelerators. Rationale: presentation-only change keeps the `app-state-machine` behaviour (and its spec) intact. The cursor is already visible outside gameplay, so Lemur picking works. Alternative: a full menu framework/state rework — rejected as out of scope.

## Risks / Trade-offs

- **Lemur picking needs a visible cursor and the default GUI viewport** → Mitigation: `GuiGlobals.initialize` wires the default mouse handling; the cursor is visible on menu/end screens (gameplay hides nothing now — `PlayerControlState` keeps it visible too). Verified by launch.
- **HUD reads game-state every frame** → Mitigation: trivial component reads + one `EntitySet.size()`; negligible. Release the set on cleanup to avoid leaks (same pattern as `VictorySystem`).
- **Default Lemur style is plain** → Accepted for M6; visual polish (glass style / theming) can come later if wanted, at the cost of Groovy.
- **UI not unit-testable** → Mitigation: factor formatting into `HudText` with `HudTextTest`; everything else is covered by the manual launch walkthrough.
