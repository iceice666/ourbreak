package com.ourbreak.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

/**
 * Game-over screen (endless survival) built with Lemur: shows how far the run got and a clickable
 * Restart button (Enter kept as a shortcut) that returns to the main menu.
 */
public class GameEndState extends BaseAppState {

    private static final String RESTART = "ourbreak.end.restart";

    private final int roundReached;

    private SimpleApplication simpleApp;
    private InputManager inputManager;
    private Node guiNode;
    private Camera camera;
    private Geometry scrim;
    private Container panel;
    private Label newBestLabel;
    private float time;

    private final ActionListener shortcuts = (name, isPressed, tpf) -> {
        if (isPressed && RESTART.equals(name)) {
            restart();
        }
    };

    public GameEndState(int roundReached) {
        if (roundReached < 1) {
            throw new IllegalArgumentException("roundReached must be at least 1");
        }
        this.roundReached = roundReached;
    }

    @Override
    protected void initialize(Application app) {
        this.simpleApp = (SimpleApplication) app;
        this.inputManager = app.getInputManager();
        this.guiNode = simpleApp.getGuiNode();
        this.camera = app.getCamera();

        scrim = UiTheme.scrim(app.getAssetManager(), camera.getWidth(), camera.getHeight());

        // Record the run and reframe the game-over as an achievement when it's a new record.
        boolean newBest = HighScore.submit(roundReached);
        int best = HighScore.best();

        panel = new Container();
        panel.setBackground(UiTheme.card(UiTheme.PANEL, 44f, 32f));

        panel.addChild(UiTheme.heading("GAME OVER", 52f, UiTheme.CORAL));
        if (newBest) {
            newBestLabel = panel.addChild(UiTheme.heading("★  NEW BEST  ★", 32f, UiTheme.GOLD));
        }
        panel.addChild(UiTheme.heading("Reached Round", 20f, UiTheme.TEXT_DIM));
        panel.addChild(UiTheme.heading(String.valueOf(roundReached), 72f, UiTheme.GOLD)); // the score shines
        if (!newBest) {
            panel.addChild(UiTheme.heading("Best: Round " + best, 18f, UiTheme.AQUA));
        }
        Label spacer = panel.addChild(new Label(""));
        spacer.setFontSize(10f);
        panel.addChild(UiTheme.primary("Restart")).addClickCommands(src -> restart());
        panel.addChild(UiTheme.heading("Enter / Esc", 14f, UiTheme.TEXT_DIM));
    }

    @Override
    public void update(float tpf) {
        if (newBestLabel != null) {
            // Pulse the record banner so the win lands emotionally.
            time += tpf;
            float k = 0.7f + 0.3f * FastMath.sin(time * 6f);
            newBestLabel.setColor(UiTheme.GOLD.mult(k));
        }
    }

    private void layout() {
        scrim.setLocalTranslation(0f, 0f, 0f);
        panel.setLocalTranslation(
                (camera.getWidth() - panel.getPreferredSize().x) / 2f,
                (camera.getHeight() + panel.getPreferredSize().y) / 2f,
                2f);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        // Restore the cursor (gameplay can leave it hidden under WSLg) so Restart is clickable.
        inputManager.setCursorVisible(true);
        layout();
        guiNode.attachChild(scrim);
        guiNode.attachChild(panel);
        inputManager.addMapping(RESTART, new KeyTrigger(KeyInput.KEY_RETURN), new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(shortcuts, RESTART);
    }

    @Override
    protected void onDisable() {
        scrim.removeFromParent();
        panel.removeFromParent();
        inputManager.removeListener(shortcuts);
        if (inputManager.hasMapping(RESTART)) {
            inputManager.deleteMapping(RESTART);
        }
    }

    private void restart() {
        getStateManager().detach(this);
        getStateManager().attach(new MainMenuState());
    }
}
