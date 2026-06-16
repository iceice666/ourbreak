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
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.ourcraft.ecs.components.PositionComponent;
import com.ourcraft.ecs.components.WeaponComponent;
import com.ourcraft.ecs.components.WeaponComponent.WeaponType;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * First-person input for an active match. WASD movement and mouse-look are both handled here (not via
 * the fly-cam) so they behave identically across platforms. The fly-cam is only borrowed for the
 * native captured-cursor look. jME's FlyByCamera (and GLFW's {@code CURSOR_DISABLED}) drive FPS look by warping
 * the cursor back to centre each frame, which WSLg/XWayland blocks (Wayland forbids apps moving the
 * cursor) — so the standard captured-cursor look never rotates. Instead we keep the cursor visible
 * and rotate from its per-frame position delta (free look, no button), with edge-steer so you can
 * keep turning when the cursor pins against a window edge (where there is no delta to read).
 * Left-click to attack, 1/2/3 or Q to switch weapon.
 */
public class PlayerControlState extends BaseAppState {

    private static final String SELECT_SWORD = "ourcraft.selectSword";
    private static final String SELECT_GUN = "ourcraft.selectGun";
    private static final String SELECT_DRONE = "ourcraft.selectDrone";
    private static final String CYCLE_WEAPON = "ourcraft.cycleWeapon";
    private static final String ATTACK = "ourcraft.attack";
    private static final String MOVE_FORWARD = "ourcraft.moveForward";
    private static final String MOVE_BACK = "ourcraft.moveBack";
    private static final String MOVE_LEFT = "ourcraft.moveLeft";
    private static final String MOVE_RIGHT = "ourcraft.moveRight";

    /** Player movement units per second at full (un-slowed) speed. */
    private static final float BASE_MOVE_SPEED = 8f;
    /** Mouse-look sensitivity (radians per pixel of cursor movement). */
    private static final float LOOK_SENSITIVITY = 0.004f;
    /** Pitch clamp so the view cannot flip over the poles. */
    private static final float PITCH_LIMIT = 1.5f;
    /** Distance (px) from a window edge at which edge-steer kicks in. */
    private static final float EDGE_MARGIN = 6f;
    /** Continuous yaw rate (rad/s) while the cursor is pinned at a left/right edge. */
    private static final float EDGE_TURN_SPEED = 1.8f;

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

    private float yaw;
    private float pitch;
    private float lastCursorX;
    private float lastCursorY;
    private boolean lookPrimed;

    private boolean moveForward;
    private boolean moveBack;
    private boolean moveLeft;
    private boolean moveRight;

    /**
     * True when the platform allows captured-cursor FPS look (real Windows / desktop Linux). False
     * under WSLg, where the cursor can't be warped, so we fall back to the visible-cursor delta look.
     */
    private final boolean nativeLook = !runningUnderWsl();

    private final ActionListener actionListener = this::onAction;

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
        swordSlash = loadSound(app, "Sound/sword-slash.wav", 0.2f);
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
        weaponSystem.close();
    }

    @Override
    protected void onEnable() {
        // We move the camera ourselves (see updateMovement), so strip the fly-cam's own movement
        // bindings — they behaved inconsistently across platforms (WASD worked under WSLg but not the
        // native Windows build). The fly-cam is then only used for the native captured-cursor look.
        for (String mapping : List.of(CameraInput.FLYCAM_STRAFELEFT, CameraInput.FLYCAM_STRAFERIGHT,
                CameraInput.FLYCAM_FORWARD, CameraInput.FLYCAM_BACKWARD,
                CameraInput.FLYCAM_RISE, CameraInput.FLYCAM_LOWER)) {
            if (inputManager.hasMapping(mapping)) {
                inputManager.deleteMapping(mapping);
            }
        }

        if (nativeLook) {
            // Real desktop: let the fly-cam do captured-cursor FPS look (cursor hidden + warped).
            flyCam.setEnabled(true);
            flyCam.setDragToRotate(false);
            flyCam.setRotationSpeed(1.5f);
            inputManager.setCursorVisible(false);
        } else {
            // WSLg: do our own delta look with the cursor visible; disable the fly-cam entirely and
            // strip its look bindings so it never tries to grab/warp the cursor (WSLg ignores that).
            flyCam.setEnabled(false);
            inputManager.setCursorVisible(true);
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
            lookPrimed = false; // first update() seeds the cursor baseline without jumping the view
        }

        guiNode.attachChild(crosshair);

        inputManager.addMapping(MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(MOVE_BACK, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(SELECT_SWORD, new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping(SELECT_GUN, new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping(SELECT_DRONE, new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping(CYCLE_WEAPON, new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(ATTACK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener,
                MOVE_FORWARD, MOVE_BACK, MOVE_LEFT, MOVE_RIGHT,
                SELECT_SWORD, SELECT_GUN, SELECT_DRONE, CYCLE_WEAPON, ATTACK);
    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(actionListener);
        for (String mapping : List.of(MOVE_FORWARD, MOVE_BACK, MOVE_LEFT, MOVE_RIGHT,
                SELECT_SWORD, SELECT_GUN, SELECT_DRONE, CYCLE_WEAPON, ATTACK)) {
            if (inputManager.hasMapping(mapping)) {
                inputManager.deleteMapping(mapping);
            }
        }
        lookPrimed = false;
        moveForward = moveBack = moveLeft = moveRight = false;
        crosshair.removeFromParent();
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
    }

    @Override
    public void update(float tpf) {
        // Coral proximity slows movement; full speed when no Coral block is in range.
        float factor = blockEffect.coralSlowFactor(PositionComponent.of(camera.getLocation()));
        updateMovement(tpf, factor);

        if (!nativeLook) {
            updateLook(tpf); // native look is driven by the fly-cam itself
        }
    }

    /** WASD moves the camera on the horizontal plane along its facing, at the (coral-slowed) speed. */
    private void updateMovement(float tpf, float speedFactor) {
        Vector3f forward = camera.getDirection().clone();
        forward.y = 0f;
        Vector3f left = camera.getLeft().clone();
        left.y = 0f;

        Vector3f velocity = new Vector3f();
        if (moveForward) velocity.addLocal(forward);
        if (moveBack) velocity.subtractLocal(forward);
        if (moveLeft) velocity.addLocal(left);
        if (moveRight) velocity.subtractLocal(left);

        if (velocity.lengthSquared() > 0f) {
            velocity.normalizeLocal().multLocal(BASE_MOVE_SPEED * speedFactor * tpf);
            camera.setLocation(camera.getLocation().add(velocity));
        }
    }

    /** WSLg/WSL2 can't warp the cursor, so captured-cursor FPS look is impossible there. */
    private static boolean runningUnderWsl() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return false; // native Windows
        }
        if (System.getenv("WSL_DISTRO_NAME") != null || System.getenv("WSL_INTEROP") != null) {
            return true;
        }
        try {
            String version = Files.readString(Path.of("/proc/version")).toLowerCase(Locale.ROOT);
            return version.contains("microsoft") || version.contains("wsl");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Free mouse-look from the visible cursor's per-frame movement (WSLg can't warp the cursor for
     * captured look). Moving the mouse rotates the view; pinning it against a left/right edge keeps
     * turning at a steady rate so you can still spin past where the cursor runs out of room.
     */
    private void updateLook(float tpf) {
        Vector2f cursor = inputManager.getCursorPosition();
        float x = cursor.x;
        float y = cursor.y; // jME cursor Y is bottom-up

        if (lookPrimed) {
            float dx = x - lastCursorX;
            float dy = y - lastCursorY;
            yaw += dx * LOOK_SENSITIVITY;   // mouse right → turn right
            pitch += dy * LOOK_SENSITIVITY; // mouse up → look up
        }
        lastCursorX = x;
        lastCursorY = y;
        lookPrimed = true;

        // Edge-steer: no cursor delta is available once it's pinned at an edge, so turn at a fixed rate.
        if (x <= EDGE_MARGIN) {
            yaw -= EDGE_TURN_SPEED * tpf;
        } else if (x >= camera.getWidth() - EDGE_MARGIN) {
            yaw += EDGE_TURN_SPEED * tpf;
        }

        pitch = FastMath.clamp(pitch, -PITCH_LIMIT, PITCH_LIMIT);
        applyLook();
    }

    private void onAction(String name, boolean isPressed, float tpf) {
        // Movement keys need both press and release to toggle the held state.
        switch (name) {
            case MOVE_FORWARD -> { moveForward = isPressed; return; }
            case MOVE_BACK -> { moveBack = isPressed; return; }
            case MOVE_LEFT -> { moveLeft = isPressed; return; }
            case MOVE_RIGHT -> { moveRight = isPressed; return; }
            default -> { /* fall through to the on-press actions below */ }
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
        // DRONE bombs a 3x3 area; SWORD sweeps a 3-cell row across the view; GUN hits the single block.
        WeaponComponent weapon = ed.getComponent(playerId, WeaponComponent.class);
        WeaponType weaponType = weapon != null ? weapon.weaponType() : WeaponType.SWORD;
        Collection<EntityId> targets = switch (weaponType) {
            case DRONE -> blockEffect.droneAreaTargets(target);
            case SWORD -> blockEffect.rowTargets(target, swordSweepAlongX());
            case GUN -> List.of(target);
        };
        // WeaponSystem gates on the ATTACK phase and applies the counter-matrix and durability;
        // destroyed entities are removed by the model-view synchronizer.
        weaponSystem.attack(playerId, targets);
        playWeaponSound(weaponType);
    }

    /** The sword sweeps left-to-right across the view: along X when facing mostly ±Z, else along Z. */
    private boolean swordSweepAlongX() {
        return Math.abs(camera.getDirection().z) >= Math.abs(camera.getDirection().x);
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
