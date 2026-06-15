package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.ourcraft.ecs.components.PositionComponent;
import com.ourcraft.ecs.components.WeaponComponent;
import com.ourcraft.ecs.components.WeaponComponent.WeaponType;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * First-person input for an active match: WASD movement and mouse-look (delegated to the fly-cam),
 * weapon switching on 1/2/3, and a left-click attack that ray-picks the block under the crosshair
 * and feeds it to {@link WeaponSystem}. Holds no rendering state of its own.
 */
public class PlayerControlState extends BaseAppState {

    private static final String SELECT_SWORD = "ourcraft.selectSword";
    private static final String SELECT_GUN = "ourcraft.selectGun";
    private static final String SELECT_DRONE = "ourcraft.selectDrone";
    private static final String ATTACK = "ourcraft.attack";

    /** Fly-cam movement units per second at full (un-slowed) speed. */
    private static final float BASE_MOVE_SPEED = 8f;

    private final EntityData ed;
    private final EntityId playerId;
    private final WeaponSystem weaponSystem;
    private final BlockEffectSystem blockEffect;

    private InputManager inputManager;
    private Camera camera;
    private Node rootNode;
    private SimpleApplication simpleApp;

    private final ActionListener listener = this::onAction;

    public PlayerControlState(
            EntityData ed,
            EntityId playerId,
            EntityId gameStateId,
            BlockEffectSystem blockEffect) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.weaponSystem = new WeaponSystem(ed, gameStateId);
        this.blockEffect = Objects.requireNonNull(blockEffect, "blockEffect");
    }

    @Override
    protected void initialize(Application app) {
        this.simpleApp = (SimpleApplication) app;
        this.inputManager = app.getInputManager();
        this.camera = app.getCamera();
        this.rootNode = simpleApp.getRootNode();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        // Fly-cam already provides WASD movement and mouse-look; capture the cursor for first-person.
        simpleApp.getFlyByCamera().setEnabled(true);
        simpleApp.getFlyByCamera().setMoveSpeed(BASE_MOVE_SPEED);
        inputManager.setCursorVisible(false);

        inputManager.addMapping(SELECT_SWORD, new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping(SELECT_GUN, new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping(SELECT_DRONE, new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping(ATTACK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(listener, SELECT_SWORD, SELECT_GUN, SELECT_DRONE, ATTACK);
    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(listener);
        for (String mapping : List.of(SELECT_SWORD, SELECT_GUN, SELECT_DRONE, ATTACK)) {
            if (inputManager.hasMapping(mapping)) {
                inputManager.deleteMapping(mapping);
            }
        }
        // Release the cursor so menu and end screens are usable.
        simpleApp.getFlyByCamera().setEnabled(false);
        inputManager.setCursorVisible(true);
    }

    @Override
    public void update(float tpf) {
        // Coral proximity slows movement; full speed when no Coral block is in range.
        float factor = blockEffect.coralSlowFactor(PositionComponent.of(camera.getLocation()));
        simpleApp.getFlyByCamera().setMoveSpeed(BASE_MOVE_SPEED * factor);
    }

    private void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }
        switch (name) {
            case SELECT_SWORD -> selectWeapon(WeaponType.SWORD);
            case SELECT_GUN -> selectWeapon(WeaponType.GUN);
            case SELECT_DRONE -> selectWeapon(WeaponType.DRONE);
            case ATTACK -> attack();
            default -> { /* ignore */ }
        }
    }

    private void selectWeapon(WeaponType type) {
        ed.setComponent(playerId, new WeaponComponent(type));
    }

    private void attack() {
        EntityId target = pickBlockUnderCrosshair();
        if (target == null) {
            return;
        }
        // DRONE bombs the 3x3 area around the crosshair block; SWORD and GUN hit only that block.
        WeaponComponent weapon = ed.getComponent(playerId, WeaponComponent.class);
        Collection<EntityId> targets = weapon != null && weapon.weaponType() == WeaponType.DRONE
                ? blockEffect.droneAreaTargets(target)
                : List.of(target);
        // WeaponSystem gates on the ATTACK phase and applies the counter-matrix and durability;
        // destroyed entities are removed by the model-view synchronizer.
        weaponSystem.attack(playerId, targets);
    }

    private EntityId pickBlockUnderCrosshair() {
        Ray ray = new Ray(camera.getLocation(), camera.getDirection());
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);

        CollisionResult hit = results.getClosestCollision();
        if (hit == null) {
            return null;
        }
        for (Spatial s = hit.getGeometry(); s != null; s = s.getParent()) {
            Long rawId = s.getUserData(ModelViewSynchronizer.ENTITY_ID_USER_DATA);
            if (rawId != null) {
                return new EntityId(rawId);
            }
        }
        return null;
    }
}
