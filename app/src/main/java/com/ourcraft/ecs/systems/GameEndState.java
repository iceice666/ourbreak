package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.scene.Node;
import com.ourcraft.ecs.components.GameResultComponent.Result;

import java.util.Objects;

/**
 * End-of-match screen showing the win/loss outcome. Placeholder GUI text only (Lemur arrives in M6);
 * ENTER returns to the main menu for a fresh match.
 */
public class GameEndState extends BaseAppState {

    private static final String RESTART = "ourcraft.end.restart";

    private final Result result;

    private InputManager inputManager;
    private Node guiNode;
    private BitmapText text;

    public GameEndState(Result result) {
        this.result = Objects.requireNonNull(result, "result");
        if (result == Result.IN_PROGRESS) {
            throw new IllegalArgumentException("end screen requires a decided result");
        }
    }

    @Override
    protected void initialize(Application app) {
        this.inputManager = app.getInputManager();
        this.guiNode = ((SimpleApplication) app).getGuiNode();

        String outcome = result == Result.WIN ? "YOU WIN" : "YOU LOSE";
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        text = new BitmapText(font);
        text.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        text.setText(outcome + "\n\n[ENTER] Back to Menu");
        text.setLocalTranslation(
                app.getCamera().getWidth() * 0.5f - text.getLineWidth() * 0.5f,
                app.getCamera().getHeight() * 0.5f + text.getHeight(),
                0f);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        guiNode.attachChild(text);
        inputManager.addMapping(RESTART, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(listener, RESTART);
    }

    @Override
    protected void onDisable() {
        text.removeFromParent();
        inputManager.removeListener(listener);
        if (inputManager.hasMapping(RESTART)) {
            inputManager.deleteMapping(RESTART);
        }
    }

    private final ActionListener listener = (name, isPressed, tpf) -> {
        if (isPressed && RESTART.equals(name)) {
            getStateManager().detach(this);
            getStateManager().attach(new MainMenuState());
        }
    };
}
