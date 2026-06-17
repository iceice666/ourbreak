package com.ourbreak.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

import java.util.List;

/**
 * Title screen built with Lemur: clickable Start Game / Exit buttons, with Enter/Esc kept as
 * keyboard shortcuts. Replaces the M4 placeholder BitmapText.
 */
public class MainMenuState extends BaseAppState {

    private static final String START = "ourbreak.menu.start";
    private static final String EXIT = "ourbreak.menu.exit";

    private SimpleApplication simpleApp;
    private InputManager inputManager;
    private Node guiNode;
    private Camera camera;
    private Geometry scrim;
    private Container panel;

    private final ActionListener shortcuts = (name, isPressed, tpf) -> {
        if (!isPressed) {
            return;
        }
        if (START.equals(name)) {
            startGame();
        } else if (EXIT.equals(name)) {
            simpleApp.stop();
        }
    };

    @Override
    protected void initialize(Application app) {
        this.simpleApp = (SimpleApplication) app;
        this.inputManager = app.getInputManager();
        this.guiNode = simpleApp.getGuiNode();
        this.camera = app.getCamera();

        scrim = UiTheme.scrim(app.getAssetManager(), camera.getWidth(), camera.getHeight());

        panel = new Container();
        panel.setBackground(UiTheme.card(UiTheme.PANEL, 44f, 32f));

        panel.addChild(UiTheme.title("OURCRAFT"));
        panel.addChild(UiTheme.heading("Endless Beach Siege", 20f, UiTheme.AQUA));
        int best = HighScore.best();
        if (best > 0) {
            panel.addChild(UiTheme.heading("Best: Round " + best, 16f, UiTheme.GOLD));
        }
        panel.addChild(spacer());

        panel.addChild(UiTheme.primary("Start Game")).addClickCommands(src -> startGame());
        panel.addChild(UiTheme.secondary("How to Play")).addClickCommands(src -> openHowToPlay());
        panel.addChild(UiTheme.subtle("Exit")).addClickCommands(src -> simpleApp.stop());
    }

    private static Label spacer() {
        Label s = new Label("");
        s.setFontSize(10f);
        return s;
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
        // Keep the OS cursor visible on the menu (WSLg hides it, and returning from gameplay can
        // leave it hidden) so the Start Game / Exit buttons stay clickable.
        inputManager.setCursorVisible(true);
        layout();
        guiNode.attachChild(scrim);
        guiNode.attachChild(panel);
        inputManager.addMapping(START, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping(EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(shortcuts, START, EXIT);
    }

    @Override
    protected void onDisable() {
        scrim.removeFromParent();
        panel.removeFromParent();
        inputManager.removeListener(shortcuts);
        for (String mapping : List.of(START, EXIT)) {
            if (inputManager.hasMapping(mapping)) {
                inputManager.deleteMapping(mapping);
            }
        }
    }

    private void startGame() {
        getStateManager().detach(this);
        getStateManager().attach(new GameplayState());
    }

    private void openHowToPlay() {
        getStateManager().detach(this);
        getStateManager().attach(new HowToPlayState());
    }
}
