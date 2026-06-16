package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Quad;

/**
 * Minimal gameplay scene decoration: a flat ground plane plus a grid overlay at the block base level,
 * so the player has a spatial reference instead of floating blocks. Placeholder visuals only — real
 * environment/block art is M8. Attached/detached by {@link GameplayState}.
 */
public class EnvironmentState extends BaseAppState {

    /** Top of the unit blocks is at y≈0.5 and the bottom at y=-0.5, so the floor sits at the block base. */
    private static final float GROUND_Y = -0.5f;
    private static final float GROUND_SIZE = 400f;
    private static final int GRID_LINES = 101;
    private static final float GRID_SPACING = 1f;

    private Node sceneRoot;
    private AssetManager assetManager;
    private Node environment;

    @Override
    protected void initialize(Application app) {
        this.sceneRoot = ((SimpleApplication) app).getRootNode();
        this.assetManager = app.getAssetManager();
        this.environment = buildEnvironment();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        sceneRoot.attachChild(environment);
    }

    @Override
    protected void onDisable() {
        environment.removeFromParent();
    }

    private Node buildEnvironment() {
        Node node = new Node("environment");

        Geometry ground = new Geometry("ground", new Quad(GROUND_SIZE, GROUND_SIZE));
        Material groundMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        groundMat.setBoolean("UseMaterialColors", true);
        ColorRGBA seaFloor = new ColorRGBA(0.12f, 0.22f, 0.26f, 1f);
        groundMat.setColor("Diffuse", seaFloor);
        groundMat.setColor("Ambient", seaFloor);
        ground.setMaterial(groundMat);
        // Lay the X-Y quad flat into the X-Z plane (normal facing up) and centre it on the origin.
        ground.rotate(-FastMath.HALF_PI, 0f, 0f);
        ground.setLocalTranslation(-GROUND_SIZE / 2f, GROUND_Y, GROUND_SIZE / 2f);
        node.attachChild(ground);

        Geometry grid = new Geometry("grid", new Grid(GRID_LINES, GRID_LINES, GRID_SPACING));
        Material gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gridMat.setColor("Color", new ColorRGBA(0.35f, 0.5f, 0.55f, 1f));
        grid.setMaterial(gridMat);
        float half = (GRID_LINES - 1) * GRID_SPACING / 2f;
        // Sit the grid just above the floor to avoid z-fighting, centred on the origin.
        grid.setLocalTranslation(-half, GROUND_Y + 0.02f, -half);
        node.attachChild(grid);

        return node;
    }
}
