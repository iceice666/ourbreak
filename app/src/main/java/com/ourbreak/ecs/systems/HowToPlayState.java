package com.ourbreak.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/**
 * "How to Play" screen reached from the main menu: a static reference of the controls, the three
 * weapons, the five block types, and the survival goal, laid out in two columns. Back / Esc returns
 * to the menu. Text is English (the bundled font is ASCII-only).
 */
public class HowToPlayState extends BaseAppState {

    private static final String BACK = "ourbreak.howto.back";

    // Palette — dark ocean dim + sandy-gold accents for high contrast over the bright beach scene.
    private static final ColorRGBA DIM = new ColorRGBA(0.04f, 0.09f, 0.13f, 0.86f);
    private static final ColorRGBA CARD = new ColorRGBA(0.09f, 0.17f, 0.22f, 0.94f);
    private static final ColorRGBA TITLE_COLOR = new ColorRGBA(1f, 0.86f, 0.5f, 1f);
    private static final ColorRGBA HEADER_COLOR = new ColorRGBA(1f, 0.78f, 0.30f, 1f);
    private static final ColorRGBA BODY_COLOR = new ColorRGBA(0.90f, 0.94f, 0.97f, 1f);

    private static final String CONTROLS =
            "Move          W A S D\n"
            + "Look          Mouse (move to look)\n"
            + "Attack        Left Click\n"
            + "Weapon        1 Sword  2 Gun  3 Drone\n"
            + "              (Q cycles weapons)\n"
            + "Confirm       Enter\n"
            + "Back / Quit   Esc";

    private static final String GOAL =
            "Endless survival. Clear the whole wall\n"
            + "before the timer runs out to reach the\n"
            + "next (harder) round. If the timer hits 0\n"
            + "with blocks left, it's Game Over.\n"
            + "Your score is the round you reach.";

    private static final String WEAPONS =
            "Sword (1)  Melee. Sweeps a 3-block row.\n"
            + "   Fast on soft walls. Bad vs Shell/Coral.\n"
            + "Gun (2)  Ranged. One-shots any one block.\n"
            + "   Clean kill for Coral & Jellyfish. No splash.\n"
            + "Drone (3)  Bombs a 3x3 area.\n"
            + "   Great vs Rock & Sand, but bombing a\n"
            + "   Jellyfish poisons you and Shells split.";

    private static final String BLOCKS =
            "Sand  1 hit. Cheap filler.\n"
            + "Coral  2 hits. Regrows the wall every 7s\n"
            + "   while alive - kill it with the Gun.\n"
            + "Shell  1 hit. Splits into 2 when hit by\n"
            + "   Sword/Drone; the Gun destroys it cleanly.\n"
            + "Rock  4 hits. Tanky, no effect. Use the Drone.\n"
            + "Jellyfish  1 hit. Drone-kill = poison (blocks\n"
            + "   turn rainbow). Kill with Gun/Sword instead.";

    private SimpleApplication simpleApp;
    private InputManager inputManager;
    private Node guiNode;
    private Camera camera;

    private Geometry backdrop;
    private Label title;
    private Container leftColumn;
    private Container rightColumn;
    private Button backButton;

    private final ActionListener shortcuts = (name, isPressed, tpf) -> {
        if (isPressed && BACK.equals(name)) {
            back();
        }
    };

    @Override
    protected void initialize(Application app) {
        this.simpleApp = (SimpleApplication) app;
        this.inputManager = app.getInputManager();
        this.guiNode = simpleApp.getGuiNode();
        this.camera = app.getCamera();

        backdrop = new Geometry("howto-dim", new Quad(camera.getWidth(), camera.getHeight()));
        Material dimMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        dimMat.setColor("Color", DIM);
        dimMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        backdrop.setMaterial(dimMat);

        title = new Label("HOW TO PLAY");
        title.setFontSize(36f);
        title.setColor(TITLE_COLOR);

        leftColumn = column("CONTROLS", CONTROLS, "GOAL", GOAL);
        rightColumn = column("WEAPONS", WEAPONS, "BLOCKS", BLOCKS);

        backButton = new Button("Back");
        backButton.setFontSize(24f);
        backButton.setColor(new ColorRGBA(0.06f, 0.1f, 0.13f, 1f));
        backButton.setBackground(new QuadBackgroundComponent(new ColorRGBA(1f, 0.78f, 0.30f, 1f)));
        backButton.addClickCommands(src -> back());
    }

    private Container column(String headerA, String bodyA, String headerB, String bodyB) {
        Container col = new Container();
        QuadBackgroundComponent bg = new QuadBackgroundComponent(CARD);
        bg.setMargin(18f, 16f); // breathing room inside the card
        col.setBackground(bg);
        col.addChild(header(headerA));
        col.addChild(body(bodyA));
        col.addChild(header(headerB));
        col.addChild(body(bodyB));
        return col;
    }

    private Label header(String text) {
        Label label = new Label(text);
        label.setFontSize(22f);
        label.setColor(HEADER_COLOR);
        return label;
    }

    private Label body(String text) {
        Label label = new Label(text);
        label.setFontSize(15f);
        label.setColor(BODY_COLOR);
        return label;
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        inputManager.setCursorVisible(true);

        float w = camera.getWidth();
        float h = camera.getHeight();

        backdrop.setLocalTranslation(0f, 0f, 0f);
        title.setLocalTranslation((w - title.getPreferredSize().x) / 2f, h - 28f, 2f);
        float columnsTop = h - 96f;
        leftColumn.setLocalTranslation(w * 0.06f, columnsTop, 2f);
        rightColumn.setLocalTranslation(w * 0.52f, columnsTop, 2f);
        backButton.setLocalTranslation((w - backButton.getPreferredSize().x) / 2f, 54f, 2f);

        guiNode.attachChild(backdrop); // behind (z=0); content sits at z=2
        guiNode.attachChild(title);
        guiNode.attachChild(leftColumn);
        guiNode.attachChild(rightColumn);
        guiNode.attachChild(backButton);

        inputManager.addMapping(BACK, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(shortcuts, BACK);
    }

    @Override
    protected void onDisable() {
        backdrop.removeFromParent();
        title.removeFromParent();
        leftColumn.removeFromParent();
        rightColumn.removeFromParent();
        backButton.removeFromParent();
        inputManager.removeListener(shortcuts);
        if (inputManager.hasMapping(BACK)) {
            inputManager.deleteMapping(BACK);
        }
    }

    private void back() {
        getStateManager().detach(this);
        getStateManager().attach(new MainMenuState());
    }
}
