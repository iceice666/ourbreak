package com.ourcraft;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.ourcraft.ecs.systems.AudioState;
import com.ourcraft.ecs.systems.MainMenuState;
import com.simsilica.lemur.GuiGlobals;

public class OurcraftGame extends SimpleApplication {

    public static void main(String[] args) {
        new OurcraftGame().start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        // WSLg hides the OS cursor as soon as it enters the window unless we explicitly keep it
        // visible; without this the Lemur menu buttons are unclickable (no pointer to click with).
        inputManager.setCursorVisible(true);

        // Lemur GUI bootstrap (default Java styling — no Groovy glass style).
        GuiGlobals.initialize(this);

        // Sunny beach sky.
        viewPort.setBackgroundColor(new ColorRGBA(0.53f, 0.81f, 0.92f, 1f));

        // Warm afternoon sun + warm ambient fill so the sand reads as a sunny beach, not a cold cave.
        DirectionalLight sun = new DirectionalLight(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        sun.setColor(new ColorRGBA(1f, 0.96f, 0.84f, 1f).mult(1.1f));
        rootNode.addLight(sun);
        rootNode.addLight(new AmbientLight(new ColorRGBA(0.62f, 0.6f, 0.52f, 1f)));

        stateManager.attach(new AudioState());
        stateManager.attach(new MainMenuState());
    }
}
