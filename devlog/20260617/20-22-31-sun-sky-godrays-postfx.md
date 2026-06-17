# Devlog — 2026-06-17 20:22:31 — `sun-sky-godrays-postfx`

> **Author**: ceil
> **Build / Version**: fidelity — lighting pass
> **Branch / Commit**: feat/destruction-juice

---

## Summary

A "make it a real place" lighting pass: a gradient sky with a visible, glowing sun and volumetric
god rays, plus directional shadows, a bloom glow and a screen vignette. No gameplay change.

---

## What I worked on

- **Visible sun** (`OurcraftGame`): there were shadows but no sun in the sky. Added a billboarded,
  overbright (`Color > 1`) sun disc (procedural `sun.png` radial sprite) parked far along the sun
  direction and kept "at infinity" each frame in `simpleUpdate`. The directional light now points
  exactly opposite the disc, so the shadows line up with where the sun sits.
- **Gradient sky** (`sky.png` + `SkyFactory` equirect dome): zenith → horizon → ground vertical
  gradient instead of a flat background colour, so the sun reads against a believable sky.
- **God rays** (`LightScatteringFilter`): volumetric light shafts streaming from the sun's screen
  position (updated each frame) — the headline "sunlight" effect.
- **Shadows / bloom / vignette**: `DirectionalLightShadowRenderer` (PCF4, soft intensity), a scene
  `BloomFilter` (also makes the sun + drone fireballs glow), and a radial `vignette.png` GUI overlay.
- **FX excluded from shadows** (`DestructionFxState`): the translucent dust/fireball FX node is set
  `ShadowMode.Off` so only the solid world casts/receives shadows.

## Why

The player asked for the game to feel like a finished work — Unreal-ish lighting where possible.
A real sky + sun + god rays is the single biggest readability/atmosphere jump for a beach scene.

## Tests / docs

- Pure rendering; no headless logic touched. `./gradlew test` green. Verified clean launch (no
  exceptions) on the native Windows GPU build.

## Watch

- God-ray density / sun size / sky colours are all easy knobs if the look needs retuning.
- The viewmodel viewport (held weapon) is a separate change; this pass is world-space only.
