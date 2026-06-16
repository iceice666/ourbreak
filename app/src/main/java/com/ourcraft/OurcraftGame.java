package com.ourcraft;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.ourcraft.ecs.systems.AudioState;
import com.ourcraft.ecs.systems.MainMenuState;

public class OurcraftGame extends SimpleApplication {

    public static void main(String[] args) {
        new OurcraftGame().start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        viewPort.setBackgroundColor(new ColorRGBA(0.1f, 0.12f, 0.15f, 1f));

        DirectionalLight sun = new DirectionalLight(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        rootNode.addLight(sun);
        rootNode.addLight(new AmbientLight(ColorRGBA.White.mult(0.3f)));

        stateManager.attach(new AudioState());
        stateManager.attach(new MainMenuState());
    }
}
