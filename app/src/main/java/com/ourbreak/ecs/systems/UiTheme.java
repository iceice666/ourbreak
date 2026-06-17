package com.ourbreak.ecs.systems;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/**
 * Shared UI design system: one palette and a few component factories so every screen (menu, end,
 * how-to-play, HUD) is visually consistent.
 *
 * <p>Colour theory: the scene is a sunny beach (blue sky, gold sand). Panels use a deep-ocean teal so
 * they recede and text pops; the primary accent is a <em>complementary</em> warm gold (opposite blue on
 * the wheel → maximum draw for the main action); aqua is an analogous harmonious highlight; coral red is
 * reserved for game-over / urgency. Hierarchy is by fill, not borders: gold-filled primary, dark
 * secondary with gold text, near-invisible subtle.
 */
public final class UiTheme {

    private UiTheme() {
    }

    // Surfaces
    public static final ColorRGBA DIM = new ColorRGBA(0.04f, 0.09f, 0.13f, 0.86f);  // full-screen scrim
    public static final ColorRGBA PANEL = new ColorRGBA(0.07f, 0.14f, 0.18f, 0.96f); // primary card
    public static final ColorRGBA CARD = new ColorRGBA(0.10f, 0.19f, 0.24f, 0.96f);  // nested / secondary
    public static final ColorRGBA PILL = new ColorRGBA(0.04f, 0.09f, 0.13f, 0.62f);  // HUD text backing

    // Accents + text
    public static final ColorRGBA GOLD = new ColorRGBA(1f, 0.78f, 0.30f, 1f);
    public static final ColorRGBA AQUA = new ColorRGBA(0.40f, 0.84f, 0.80f, 1f);
    public static final ColorRGBA CORAL = new ColorRGBA(1f, 0.45f, 0.40f, 1f);
    public static final ColorRGBA TEXT = new ColorRGBA(0.90f, 0.94f, 0.97f, 1f);
    public static final ColorRGBA TEXT_DIM = new ColorRGBA(0.62f, 0.72f, 0.78f, 1f);
    public static final ColorRGBA INK = new ColorRGBA(0.05f, 0.09f, 0.12f, 1f); // dark text on gold

    private static final float BUTTON_W = 280f;
    private static final float BUTTON_H = 52f;

    /** Full-screen translucent scrim that darkens the live scene so foreground UI reads clearly. */
    public static Geometry scrim(AssetManager assetManager, float width, float height) {
        Geometry g = new Geometry("ui-scrim", new Quad(width, height));
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", DIM);
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        g.setMaterial(m);
        return g;
    }

    /** A rounded-feel dark backing for HUD text so it stays legible over the bright beach. */
    public static QuadBackgroundComponent pill() {
        QuadBackgroundComponent bg = new QuadBackgroundComponent(PILL);
        bg.setMargin(10f, 5f);
        return bg;
    }

    public static QuadBackgroundComponent card(ColorRGBA color, float marginX, float marginY) {
        QuadBackgroundComponent bg = new QuadBackgroundComponent(color);
        bg.setMargin(marginX, marginY);
        return bg;
    }

    public static Label title(String text) {
        Label label = new Label(text);
        label.setFontSize(60f);
        label.setColor(GOLD);
        return label;
    }

    public static Label heading(String text, float size, ColorRGBA color) {
        Label label = new Label(text);
        label.setFontSize(size);
        label.setColor(color);
        return label;
    }

    public static Button primary(String text) {
        return button(text, GOLD, INK, 26f);
    }

    public static Button secondary(String text) {
        return button(text, CARD, GOLD, 22f);
    }

    public static Button subtle(String text) {
        return button(text, new ColorRGBA(0.10f, 0.16f, 0.20f, 0.55f), TEXT_DIM, 20f);
    }

    private static Button button(String text, ColorRGBA fill, ColorRGBA textColor, float fontSize) {
        Button b = new Button(text);
        b.setFontSize(fontSize);
        b.setColor(textColor);
        b.setTextHAlignment(HAlignment.Center);
        b.setTextVAlignment(VAlignment.Center);
        b.setPreferredSize(new Vector3f(BUTTON_W, BUTTON_H, 0f));

        final ColorRGBA base = fill.clone();
        final ColorRGBA hover = fill.mult(1.18f);
        hover.a = fill.a;
        QuadBackgroundComponent bg = new QuadBackgroundComponent(base);
        b.setBackground(bg);
        // Hover feedback (professional affordance): brighten the fill while the pointer is over it.
        b.addCommands(Button.ButtonAction.HighlightOn, src -> bg.setColor(hover));
        b.addCommands(Button.ButtonAction.HighlightOff, src -> bg.setColor(base));
        return b;
    }
}
