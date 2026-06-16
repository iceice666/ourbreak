package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.CameraInput;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
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
 * First-person input for an active match. WASD movement is delegated to the fly-cam, but mouse-look
 * is handled here so the cursor stays visible — jME's FlyByCamera hides/grabs the cursor to look,
 * which WSLg/XWayland does not deliver relative motion for. Instead: hold the RIGHT mouse button and
 * move to look around (cursor stays visible), left-click to attack, 1/2/3 to switch weapon.
 */
public class PlayerControlState extends BaseAppState {

    private static final String SELECT_SWORD = "ourcraft.selectSword";
    private static final String SELECT_GUN = "ourcraft.selectGun";
    private static final String SELECT_DRONE = "ourcraft.selectDrone";
    private static final String CYCLE_WEAPON = "ourcraft.cycleWeapon";
    private static final String ATTACK = "ourcraft.attack";
    private static final String LOOK_DRAG = "ourcraft.lookDrag";
    private static final String LOOK_X_NEG = "ourcraft.lookXNeg";
    private static final String LOOK_X_POS = "ourcraft.lookXPos";
    private static final String LOOK_Y_NEG = "ourcraft.lookYNeg";
    private static final String LOOK_Y_POS = "ourcraft.lookYPos";

    /** Fly-cam movement units per second at full (un-slowed) speed. */
    private static final float BASE_MOVE_SPEED = 8f;
    /** Mouse-look sensitivity (radians per unit of mouse-axis motion). */
    private static final float LOOK_SENSITIVITY = 3f;
    /** Pitch clamp so the view cannot flip over the poles. */
    private static final float PITCH_LIMIT = 1.5f;

    private final EntityData ed;
    private final EntityId playerId;
    private final WeaponSystem weaponSystem;
    private final BlockEffectSystem blockEffect;

    private InputManager inputManager;
    private Camera camera;
    private Node rootNode;
    private Node guiNode;
    private SimpleApplication simpleApp;
    private FlyByCamera flyCam;
    private BitmapText crosshair;
    private AudioNode droneBoom;
    private AudioNode swordSlash;

    private boolean looking;
    private float yaw;
    private float pitch;

    private final ActionListener actionListener = this::onAction;
    private final AnalogListener lookListener = this::onLook;

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
        this.guiNode = simpleApp.getGuiNode();
        this.flyCam = simpleApp.getFlyByCamera();

        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        crosshair = new BitmapText(font);
        crosshair.setText("+");
        crosshair.setLocalTranslation(
                camera.getWidth() / 2f - crosshair.getLineWidth() / 2f,
                camera.getHeight() / 2f + crosshair.getLineHeight() / 2f,
                0f);

        droneBoom = loadSound(app, "Sound/drone-boom.wav", 0.8f);
        swordSlash = loadSound(app, "Sound/sword-slash.wav", 0.4f);
    }

    private static AudioNode loadSound(Application app, String asset, float volume) {
        try {
            AudioNode node = new AudioNode(app.getAssetManager(), asset, AudioData.DataType.Buffer);
            node.setPositional(false);
            node.setVolume(volume);
            return node;
        } catch (RuntimeException e) {
            System.err.println("[audio] sound unavailable (" + asset + "): " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        // Keep the fly-cam for WASD movement only; dragToRotate(true) stops it from hiding the cursor.
        flyCam.setEnabled(true);
        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(BASE_MOVE_SPEED);
        inputManager.setCursorVisible(true);

        // Strip the fly-cam's own look controls so it never grabs the cursor; we do mouse-look here.
        for (String mapping : List.of(CameraInput.FLYCAM_LEFT, CameraInput.FLYCAM_RIGHT,
                CameraInput.FLYCAM_UP, CameraInput.FLYCAM_DOWN, CameraInput.FLYCAM_ROTATEDRAG)) {
            if (inputManager.hasMapping(mapping)) {
                inputManager.deleteMapping(mapping);
            }
        }

        // Start looking roughly toward the play area (facing -Z, tilted slightly down).
        yaw = 0f;
        pitch = -0.1f;
        applyLook();

        guiNode.attachChild(crosshair);

        inputManager.addMapping(LOOK_DRAG, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping(LOOK_X_NEG, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(LOOK_X_POS, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping(LOOK_Y_NEG, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(LOOK_Y_POS, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addListener(lookListener, LOOK_X_NEG, LOOK_X_POS, LOOK_Y_NEG, LOOK_Y_POS);

        inputManager.addMapping(SELECT_SWORD, new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping(SELECT_GUN, new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping(SELECT_DRONE, new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping(CYCLE_WEAPON, new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(ATTACK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener,
                LOOK_DRAG, SELECT_SWORD, SELECT_GUN, SELECT_DRONE, CYCLE_WEAPON, ATTACK);
    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(actionListener);
        inputManager.removeListener(lookListener);
        for (String mapping : List.of(LOOK_DRAG, LOOK_X_NEG, LOOK_X_POS, LOOK_Y_NEG, LOOK_Y_POS,
                SELECT_SWORD, SELECT_GUN, SELECT_DRONE, CYCLE_WEAPON, ATTACK)) {
            if (inputManager.hasMapping(mapping)) {
                inputManager.deleteMapping(mapping);
            }
        }
        looking = false;
        crosshair.removeFromParent();
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
    }

    @Override
    public void update(float tpf) {
        // Coral proximity slows movement; full speed when no Coral block is in range.
        float factor = blockEffect.coralSlowFactor(PositionComponent.of(camera.getLocation()));
        flyCam.setMoveSpeed(BASE_MOVE_SPEED * factor);
    }

    private void onAction(String name, boolean isPressed, float tpf) {
        if (LOOK_DRAG.equals(name)) {
            looking = isPressed;
            return;
        }
        if (!isPressed) {
            return;
        }
        switch (name) {
            case SELECT_SWORD -> selectWeapon(WeaponType.SWORD);
            case SELECT_GUN -> selectWeapon(WeaponType.GUN);
            case SELECT_DRONE -> selectWeapon(WeaponType.DRONE);
            case CYCLE_WEAPON -> cycleWeapon();
            case ATTACK -> attack();
            default -> { /* ignore */ }
        }
    }

    private void cycleWeapon() {
        WeaponComponent weapon = ed.getComponent(playerId, WeaponComponent.class);
        WeaponType[] types = WeaponType.values();
        int current = weapon != null ? weapon.weaponType().ordinal() : -1;
        selectWeapon(types[(current + 1) % types.length]);
    }

    private void onLook(String name, float value, float tpf) {
        if (!looking) {
            return;
        }
        switch (name) {
            case LOOK_X_NEG -> yaw -= value * LOOK_SENSITIVITY;
            case LOOK_X_POS -> yaw += value * LOOK_SENSITIVITY;
            case LOOK_Y_NEG -> pitch -= value * LOOK_SENSITIVITY;
            case LOOK_Y_POS -> pitch += value * LOOK_SENSITIVITY;
            default -> { /* ignore */ }
        }
        pitch = FastMath.clamp(pitch, -PITCH_LIMIT, PITCH_LIMIT);
        applyLook();
    }

    private void applyLook() {
        camera.setRotation(new Quaternion().fromAngles(pitch, yaw, 0f));
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
        WeaponType weaponType = weapon != null ? weapon.weaponType() : WeaponType.SWORD;
        Collection<EntityId> targets = weaponType == WeaponType.DRONE
                ? blockEffect.droneAreaTargets(target)
                : List.of(target);
        // WeaponSystem gates on the ATTACK phase and applies the counter-matrix and durability;
        // destroyed entities are removed by the model-view synchronizer.
        weaponSystem.attack(playerId, targets);
        playWeaponSound(weaponType);
    }

    private void playWeaponSound(WeaponType weaponType) {
        AudioNode sound = switch (weaponType) {
            case SWORD -> swordSlash;
            case DRONE -> droneBoom;
            case GUN -> null; // gun sound TBD
        };
        if (sound != null) {
            sound.playInstance();
        }
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
