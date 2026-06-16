package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

import java.util.List;

/**
 * Title screen built with Lemur: clickable Start Game / Exit buttons, with Enter/Esc kept as
 * keyboard shortcuts. Replaces the M4 placeholder BitmapText.
 */
public class MainMenuState extends BaseAppState {

    private static final String START = "ourcraft.menu.start";
    private static final String EXIT = "ourcraft.menu.exit";

    private SimpleApplication simpleApp;
    private InputManager inputManager;
    private Node guiNode;
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

        panel = new Container();
        Label title = panel.addChild(new Label("ourcraft"));
        title.setFontSize(36f);
        panel.addChild(new Button("Start Game")).addClickCommands(src -> startGame());
        panel.addChild(new Button("Exit")).addClickCommands(src -> simpleApp.stop());

        panel.setLocalTranslation(
                (app.getCamera().getWidth() - panel.getPreferredSize().x) / 2f,
                (app.getCamera().getHeight() + panel.getPreferredSize().y) / 2f,
                0f);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        guiNode.attachChild(panel);
        inputManager.addMapping(START, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping(EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(shortcuts, START, EXIT);
    }

    @Override
    protected void onDisable() {
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
}
