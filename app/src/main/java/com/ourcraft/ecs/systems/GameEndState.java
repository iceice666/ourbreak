package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.scene.Node;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

import java.util.Objects;

/**
 * End-of-match screen built with Lemur: shows the win/loss outcome and a clickable Restart button
 * (Enter kept as a shortcut) that returns to the main menu. Replaces the M4 placeholder BitmapText.
 */
public class GameEndState extends BaseAppState {

    private static final String RESTART = "ourcraft.end.restart";

    private final Result result;

    private SimpleApplication simpleApp;
    private InputManager inputManager;
    private Node guiNode;
    private Container panel;

    private final ActionListener shortcuts = (name, isPressed, tpf) -> {
        if (isPressed && RESTART.equals(name)) {
            restart();
        }
    };

    public GameEndState(Result result) {
        this.result = Objects.requireNonNull(result, "result");
        if (result == Result.IN_PROGRESS) {
            throw new IllegalArgumentException("end screen requires a decided result");
        }
    }

    @Override
    protected void initialize(Application app) {
        this.simpleApp = (SimpleApplication) app;
        this.inputManager = app.getInputManager();
        this.guiNode = simpleApp.getGuiNode();

        panel = new Container();
        Label outcome = panel.addChild(new Label(result == Result.WIN ? "YOU WIN" : "YOU LOSE"));
        outcome.setFontSize(36f);
        panel.addChild(new Button("Restart")).addClickCommands(src -> restart());

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
        inputManager.addMapping(RESTART, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(shortcuts, RESTART);
    }

    @Override
    protected void onDisable() {
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
