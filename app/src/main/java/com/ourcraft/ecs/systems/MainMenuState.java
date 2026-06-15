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

import java.util.List;

/**
 * Title screen. Placeholder GUI text only (Lemur arrives in M6): ENTER starts a match, ESCAPE exits.
 */
public class MainMenuState extends BaseAppState {

    private static final String START = "ourcraft.menu.start";
    private static final String EXIT = "ourcraft.menu.exit";

    private SimpleApplication simpleApp;
    private InputManager inputManager;
    private Node guiNode;
    private BitmapText text;

    private final ActionListener listener = this::onAction;

    @Override
    protected void initialize(Application app) {
        this.simpleApp = (SimpleApplication) app;
        this.inputManager = app.getInputManager();
        this.guiNode = simpleApp.getGuiNode();

        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        text = new BitmapText(font);
        text.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        text.setText("ourcraft\n\n[ENTER] Start Game\n[ESC] Exit");
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
        inputManager.addMapping(START, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping(EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(listener, START, EXIT);
    }

    @Override
    protected void onDisable() {
        text.removeFromParent();
        inputManager.removeListener(listener);
        for (String mapping : List.of(START, EXIT)) {
            if (inputManager.hasMapping(mapping)) {
                inputManager.deleteMapping(mapping);
            }
        }
    }

    private void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }
        if (START.equals(name)) {
            getStateManager().detach(this);
            getStateManager().attach(new GameplayState());
        } else if (EXIT.equals(name)) {
            simpleApp.stop();
        }
    }
}
