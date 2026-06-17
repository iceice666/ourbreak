package com.ourbreak.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.PhaseComponent;
import com.ourbreak.ecs.components.PhaseComponent.Phase;
import com.ourbreak.ecs.components.PositionComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The mascot the NPC walls off and the player tears down to: a little cartoon crab built from jME
 * primitives (round shell, big claws, stalk eyes, tiny legs, blush). It sits at the origin (where the
 * mascot entity lives) facing the player's spawn, idly bobbing and waving its claws so the courtyard
 * has a charming payoff once the wall comes down. Placeholder art until a real model (M8).
 */
public class MascotState extends BaseAppState {

    private static final float GROUND_Y = -0.5f; // crab stands on the sand (block base height)
    private static final float LOS_CHECK_INTERVAL = 0.25f;
    private static final float FLEE_SPEED = 6f;
    private static final float FLEE_DURATION = 1.7f;
    private static final float FLEE_SHRINK = 0.45f; // last seconds spent scaling away

    private enum Mode { IDLE, FLEEING, GONE }

    private static final ColorRGBA SHELL = new ColorRGBA(0.94f, 0.38f, 0.29f, 1f);
    private static final ColorRGBA SHELL_DK = new ColorRGBA(0.82f, 0.29f, 0.22f, 1f);
    private static final ColorRGBA BELLY = new ColorRGBA(0.99f, 0.72f, 0.55f, 1f);
    private static final ColorRGBA WHITE = new ColorRGBA(0.98f, 0.98f, 1f, 1f);
    private static final ColorRGBA BLACK = new ColorRGBA(0.07f, 0.07f, 0.09f, 1f);
    private static final ColorRGBA BLUSH = new ColorRGBA(1f, 0.56f, 0.5f, 1f);

    private final EntityData ed;
    private final EntityId gameStateId;

    private Node sceneRoot;
    private AssetManager assetManager;
    private Camera camera;
    private EntitySet blocks;

    private Node crab;
    private Node leftClaw;
    private Node rightClaw;
    private float time;

    private Mode mode = Mode.IDLE;
    private float losTimer;
    private final Vector3f fleePos = new Vector3f();
    private final Vector3f fleeDir = new Vector3f(0f, 0f, 1f);
    private float fleeTime;

    public MascotState(EntityData ed, EntityId gameStateId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
    }

    @Override
    protected void initialize(Application app) {
        this.sceneRoot = ((SimpleApplication) app).getRootNode();
        this.assetManager = app.getAssetManager();
        this.camera = app.getCamera();
        this.blocks = ed.getEntities(BlockComponent.class, PositionComponent.class);
        this.crab = buildCrab();
    }

    @Override
    protected void cleanup(Application app) {
        blocks.release();
    }

    @Override
    protected void onEnable() {
        sceneRoot.attachChild(crab);
    }

    @Override
    protected void onDisable() {
        crab.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        time += tpf;

        PhaseComponent phase = ed.getComponent(gameStateId, PhaseComponent.class);
        Phase ph = phase == null ? null : phase.phase();

        // A new round rebuilds the wall — the crab scuttles back to the centre good as new.
        if (ph == Phase.BUILD && mode != Mode.IDLE) {
            resetToIdle();
        }

        switch (mode) {
            case IDLE -> {
                idleAnimate();
                // Once the player breaks a line of sight to the crab during ATTACK, it panics and bolts.
                if (ph == Phase.ATTACK) {
                    losTimer += tpf;
                    if (losTimer >= LOS_CHECK_INTERVAL) {
                        losTimer = 0f;
                        if (exposedToPlayer()) {
                            startFleeing();
                        }
                    }
                }
            }
            case FLEEING -> fleeAnimate(tpf);
            case GONE -> { /* stay hidden until the next round's BUILD resets us */ }
        }
    }

    private void idleAnimate() {
        float bob = FastMath.sin(time * 2.2f) * 0.05f;
        crab.setLocalTranslation(0f, GROUND_Y + bob, 0f);
        float wave = 0.22f * FastMath.sin(time * 3f);
        if (leftClaw != null) {
            leftClaw.setLocalRotation(new Quaternion().fromAngles(0f, 0f, wave));
        }
        if (rightClaw != null) {
            rightClaw.setLocalRotation(new Quaternion().fromAngles(0f, 0f, -wave));
        }
    }

    private void startFleeing() {
        mode = Mode.FLEEING;
        fleeTime = 0f;
        fleePos.set(0f, 0f, 0f);
        // Bolt away from the player along the ground.
        Vector3f away = crab.getWorldTranslation().subtract(camera.getLocation());
        away.y = 0f;
        if (away.lengthSquared() < 1e-4f) {
            away.set(0f, 0f, 1f);
        }
        fleeDir.set(away.normalizeLocal());
    }

    private void fleeAnimate(float tpf) {
        fleeTime += tpf;
        fleePos.addLocal(fleeDir.mult(FLEE_SPEED * tpf));

        float hop = FastMath.abs(FastMath.sin(fleeTime * 14f)) * 0.12f; // scuttling hops
        crab.setLocalTranslation(fleePos.x, GROUND_Y + hop, fleePos.z);

        float yaw = FastMath.atan2(fleeDir.x, fleeDir.z);
        float wobble = 0.16f * FastMath.sin(fleeTime * 20f); // panic wobble
        crab.setLocalRotation(new Quaternion().fromAngles(0f, yaw, wobble));

        float flail = 0.55f * FastMath.sin(fleeTime * 22f); // frantic claw flailing
        if (leftClaw != null) {
            leftClaw.setLocalRotation(new Quaternion().fromAngles(0f, 0f, flail));
        }
        if (rightClaw != null) {
            rightClaw.setLocalRotation(new Quaternion().fromAngles(0f, 0f, -flail));
        }

        // Shrink away over the last stretch, then vanish.
        float remaining = FLEE_DURATION - fleeTime;
        if (remaining < FLEE_SHRINK) {
            crab.setLocalScale(Math.max(0f, remaining / FLEE_SHRINK));
        }
        if (fleeTime >= FLEE_DURATION) {
            crab.removeFromParent();
            mode = Mode.GONE;
        }
    }

    private void resetToIdle() {
        fleePos.set(0f, 0f, 0f);
        crab.setLocalScale(1f);
        crab.setLocalRotation(new Quaternion());
        crab.setLocalTranslation(0f, GROUND_Y, 0f);
        if (crab.getParent() == null) {
            sceneRoot.attachChild(crab);
        }
        mode = Mode.IDLE;
        losTimer = 0f;
    }

    /** True when nothing blocks the straight line from the crab to the player (a hole has been opened). */
    private boolean exposedToPlayer() {
        blocks.applyChanges();
        Set<String> occupied = new HashSet<>();
        for (Entity block : blocks) {
            PositionComponent p = block.get(PositionComponent.class);
            occupied.add(cellKey(Math.round(p.x()), Math.round(p.y()), Math.round(p.z())));
        }

        Vector3f from = new Vector3f(0f, 0.5f, 0f);
        Vector3f to = camera.getLocation();
        Vector3f dir = to.subtract(from);
        float dist = dir.length();
        if (dist < 0.001f) {
            return true;
        }
        dir.divideLocal(dist);
        for (float t = 0.6f; t < dist - 0.6f; t += 0.3f) {
            Vector3f p = from.add(dir.mult(t));
            if (occupied.contains(cellKey(Math.round(p.x), Math.round(p.y), Math.round(p.z)))) {
                return false; // a block still occludes the crab
            }
        }
        return true;
    }

    private static String cellKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private Node buildCrab() {
        Node node = new Node("mascot-crab");
        node.setLocalTranslation(0f, GROUND_Y, 0f);

        // Big rounded chibi shell (rounder + a touch taller = cuter) and a soft belly patch.
        node.attachChild(part("crab-shell", new Sphere(24, 24, 0.58f), SHELL,
                0f, 0.54f, 0f, 1.30f, 1.02f, 1.16f));
        node.attachChild(part("crab-belly", new Sphere(18, 18, 0.45f), BELLY,
                0f, 0.44f, 0.38f, 1.06f, 0.84f, 0.52f));

        for (int s = -1; s <= 1; s += 2) {
            // Short stalks with big shiny kawaii eyes (white eyeball + big pupil + sparkle highlight).
            node.attachChild(part("crab-stalk", new Box(0.05f, 0.10f, 0.05f), SHELL_DK,
                    s * 0.20f, 0.96f, 0.30f, 1f, 1f, 1f));
            node.attachChild(part("crab-eye", new Sphere(18, 18, 0.19f), WHITE,
                    s * 0.20f, 1.12f, 0.34f, 1f, 1f, 1f));
            node.attachChild(part("crab-pupil", new Sphere(14, 14, 0.12f), BLACK,
                    s * 0.20f, 1.12f, 0.45f, 1f, 1f, 1f));
            node.attachChild(part("crab-sparkle", new Sphere(10, 10, 0.045f), WHITE,
                    s * 0.16f, 1.18f, 0.53f, 1f, 1f, 1f));
            // Little curved antennae with bobble tips.
            Geometry antenna = part("crab-antenna", new Box(0.018f, 0.16f, 0.018f), SHELL_DK,
                    s * 0.10f, 1.22f, 0.18f, 1f, 1f, 1f);
            antenna.rotate(0f, 0f, s * -0.35f);
            node.attachChild(antenna);
            node.attachChild(part("crab-antenna-tip", new Sphere(8, 8, 0.045f), SHELL,
                    s * 0.20f, 1.40f, 0.18f, 1f, 1f, 1f));
            // Rosy blush.
            node.attachChild(part("crab-blush", new Sphere(12, 12, 0.11f), BLUSH,
                    s * 0.42f, 0.60f, 0.52f, 1f, 0.7f, 0.3f));
            // Three little legs per side, splayed out and down to the sand.
            for (int i = -1; i <= 1; i++) {
                Geometry leg = part("crab-leg", new Box(0.05f, 0.22f, 0.06f), SHELL_DK,
                        s * 0.66f, 0.24f, i * 0.28f, 1f, 1f, 1f);
                leg.rotate(0f, 0f, s * -0.6f);
                node.attachChild(leg);
            }
            // Big chunky claw on a pivot node so it can wave.
            Node claw = buildClaw(s);
            node.attachChild(claw);
            if (s < 0) {
                leftClaw = claw;
            } else {
                rightClaw = claw;
            }
        }

        // A small happy smile.
        node.attachChild(part("crab-mouth", new Box(0.13f, 0.022f, 0.02f), BLACK,
                0f, 0.6f, 0.68f, 1f, 1f, 1f));
        return node;
    }

    /** One big chunky claw (upper arm + round pincer with two tips) hung off a pivot at the shoulder. */
    private Node buildClaw(int side) {
        Node claw = new Node("crab-claw");
        claw.setLocalTranslation(side * 0.58f, 0.5f, 0.24f);

        claw.attachChild(part("claw-arm", new Box(0.20f, 0.10f, 0.10f), SHELL_DK,
                side * 0.16f, 0f, 0f, 1f, 1f, 1f));
        claw.attachChild(part("claw-bulb", new Sphere(18, 18, 0.30f), SHELL,
                side * 0.46f, 0.02f, 0.06f, 1.15f, 1.4f, 0.95f));
        claw.attachChild(part("claw-tip-top", new Box(0.20f, 0.06f, 0.09f), SHELL,
                side * 0.7f, 0.15f, 0.12f, 1f, 1f, 1f));
        claw.attachChild(part("claw-tip-bot", new Box(0.20f, 0.06f, 0.09f), SHELL,
                side * 0.7f, -0.05f, 0.12f, 1f, 1f, 1f));
        return claw;
    }

    private Geometry part(String name, Mesh mesh, ColorRGBA color,
                          float x, float y, float z, float sx, float sy, float sz) {
        Geometry g = new Geometry(name, mesh);
        Material m = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse", color);
        m.setColor("Ambient", color);
        g.setMaterial(m);
        g.setLocalScale(sx, sy, sz);
        g.setLocalTranslation(x, y, z);
        return g;
    }
}
