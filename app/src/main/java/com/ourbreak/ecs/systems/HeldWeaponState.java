package com.ourbreak.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.ourbreak.ecs.components.WeaponComponent;
import com.ourbreak.ecs.components.WeaponComponent.WeaponType;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * First-person "held weapon" viewmodel: the equipped weapon's 3D model (built procedurally in
 * {@link WeaponModels}) is rendered in a dedicated viewport that clears only depth, so it always sits in
 * the player's hand without clipping into walls. It bobs while you move and swings when you attack, like
 * Minecraft / a typical FPS. Purely cosmetic — no gameplay change.
 */
public class HeldWeaponState extends BaseAppState {

    /** Resting position of the weapon in camera space (right, down, forward). */
    private static final Vector3f BASE_POS = new Vector3f(0.21f, -0.17f, -0.72f);
    /** Overall scale of the held models (smaller = the weapon reads as further from the eye). */
    private static final float VIEWMODEL_SCALE = 0.78f;
    private static final float SWING_DURATION = 0.3f;

    private final EntityData ed;
    private final EntityId playerId;
    private final Map<WeaponType, Node> models = new EnumMap<>(WeaponType.class);

    private AssetManager assetManager;
    private Camera mainCamera;
    private RenderManager renderManager;

    private Camera vmCam;
    private ViewPort viewModelView;
    private Node viewModelNode;
    private Node holder;

    private WeaponType shown;
    private Node droneRotors;
    private float bobTime;
    private float swingTime;
    private final Vector3f lastCamPos = new Vector3f();

    public HeldWeaponState(EntityData ed, EntityId playerId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    @Override
    protected void initialize(Application app) {
        this.assetManager = app.getAssetManager();
        this.mainCamera = app.getCamera();
        this.renderManager = app.getRenderManager();

        viewModelNode = new Node("viewmodel-root");
        holder = new Node("viewmodel-holder");
        holder.setLocalScale(VIEWMODEL_SCALE);
        viewModelNode.attachChild(holder);

        // The viewmodel scene has its own lights (lights don't cross viewports): a key light from the
        // upper-front-right plus warm ambient so the low-poly models read with clean shading.
        DirectionalLight key = new DirectionalLight(
                new Vector3f(-0.4f, -0.5f, -0.8f).normalizeLocal(),
                new ColorRGBA(1f, 0.97f, 0.9f, 1f).mult(1.15f));
        viewModelNode.addLight(key);
        viewModelNode.addLight(new AmbientLight(new ColorRGBA(0.5f, 0.5f, 0.55f, 1f)));

        // A static camera looking down -Z; the weapon is parked in front of it so it's locked to the
        // screen regardless of where the player aims. Clearing depth only draws it over the world.
        int w = mainCamera.getWidth();
        int h = mainCamera.getHeight();
        vmCam = new Camera(w, h);
        vmCam.setFrustumPerspective(45f, (float) w / h, 0.04f, 10f);
        vmCam.setLocation(new Vector3f(0f, 0f, 0f));
        vmCam.lookAt(new Vector3f(0f, 0f, -1f), Vector3f.UNIT_Y);
        viewModelView = renderManager.createMainView("ViewModel", vmCam);
        viewModelView.setClearFlags(false, true, false);
        viewModelView.attachScene(viewModelNode);

        models.put(WeaponType.SWORD, WeaponModels.sword(assetManager));
        models.put(WeaponType.GUN, WeaponModels.gun(assetManager));
        models.put(WeaponType.DRONE, WeaponModels.drone(assetManager));

        lastCamPos.set(mainCamera.getLocation());
    }

    @Override
    protected void cleanup(Application app) {
        if (viewModelView != null) {
            renderManager.removeMainView(viewModelView);
        }
    }

    @Override
    protected void onEnable() {
        if (viewModelView != null) {
            viewModelView.setEnabled(true);
        }
    }

    @Override
    protected void onDisable() {
        if (viewModelView != null) {
            viewModelView.setEnabled(false);
        }
    }

    /** Trigger a swing animation (called by the player's attack, even on a miss — like Minecraft). */
    public void swing() {
        swingTime = SWING_DURATION;
    }

    @Override
    public void update(float tpf) {
        WeaponComponent weapon = ed.getComponent(playerId, WeaponComponent.class);
        if (weapon == null) {
            return;
        }
        WeaponType type = weapon.weaponType();
        if (type != shown) {
            holder.detachAllChildren();
            Node model = models.get(type);
            holder.attachChild(model);
            droneRotors = type == WeaponType.DRONE ? (Node) model.getChild("rotors") : null;
            shown = type;
        }

        // Walk-bob: a figure-of-eight sway, wider/faster while the camera is actually moving.
        Vector3f camPos = mainCamera.getLocation();
        boolean moving = camPos.distanceSquared(lastCamPos) > 1.0e-5f;
        lastCamPos.set(camPos);
        bobTime += tpf * (moving ? 8f : 2.5f);
        float ampX = moving ? 0.016f : 0.005f;
        float ampY = moving ? 0.013f : 0.004f;
        float bobX = FastMath.sin(bobTime) * ampX;
        float bobY = FastMath.abs(FastMath.cos(bobTime)) * ampY;

        // Attack motion, eased in and out over the swing window (s: 0→1→0). Each weapon moves
        // differently: the sword slashes diagonally across the view (roll-led, not a forward flop), the
        // gun recoils (muzzle up + punch back toward the eye), the drone gives a small forward dip.
        Quaternion swingRot = new Quaternion();
        float swingPush = 0f;
        if (swingTime > 0f) {
            swingTime = Math.max(0f, swingTime - tpf);
            float s = FastMath.sin((1f - swingTime / SWING_DURATION) * FastMath.PI);
            switch (shown) {
                case GUN -> {
                    swingRot.fromAngles(s * 0.8f, 0f, 0f); // muzzle kicks up
                    swingPush = s * 0.08f;                 // and back toward the camera
                }
                case SWORD -> {
                    // Downward diagonal chop: the blade drives forward-and-down from the upper-left rest
                    // toward the lower-right (pitch-led, not a windmill around the view axis).
                    swingRot.fromAngles(-s * 0.7f, -s * 0.3f, -s * 0.55f);
                    swingPush = s * 0.05f;
                }
                case DRONE -> {
                    swingRot.fromAngles(-s * 0.45f, 0f, 0f); // small forward dip
                    swingPush = s * 0.05f;
                }
            }
        }

        holder.setLocalTranslation(BASE_POS.x + bobX, BASE_POS.y - bobY, BASE_POS.z + swingPush);
        holder.setLocalRotation(swingRot);

        if (droneRotors != null) {
            droneRotors.rotate(0f, tpf * 40f, 0f); // blades whirring
        }

        // This scene isn't under the app's rootNode, so advance its transforms ourselves.
        viewModelNode.updateLogicalState(tpf);
        viewModelNode.updateGeometricState();
    }
}
