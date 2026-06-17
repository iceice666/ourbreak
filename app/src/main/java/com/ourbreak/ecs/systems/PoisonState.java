package com.ourbreak.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.BlockComponent.BlockType;
import com.ourbreak.ecs.components.WeaponComponent;
import com.ourbreak.ecs.components.WeaponComponent.WeaponType;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Jellyfish poison: blowing up a Jellyfish with the DRONE poisons the player for +5s (capped at 10s).
 * While poisoned — no matter which weapon is held — every real block in the scene flickers through random
 * rainbow colours, so block types become unreadable (you can't tell Shell from Rock from Sand). A draining
 * bar on the GUI shows the remaining poison. Killing a Jellyfish with the Gun or Sword does NOT poison;
 * this punishes brainlessly droning the wall, which is exactly when Jellyfish are most tempting to nuke.
 *
 * <p>Gun/Sword immunity is on the <em>trigger</em> (only the drone poisons), not on the effect — once
 * poisoned the hallucination applies regardless of the currently equipped weapon.
 */
public class PoisonState extends BaseAppState {

    private static final float POISON_PER_KILL = 5.0f;
    private static final float POISON_MAX = 10.0f;
    private static final float RECOLOR_INTERVAL = 0.12f; // re-scramble block colours this often

    private static final float BAR_WIDTH = 240f;
    private static final float BAR_HEIGHT = 18f;
    private static final float BAR_MARGIN = 48f; // up from the bottom edge, clear of the HUD

    private static final ColorRGBA[] RAINBOW = {
            new ColorRGBA(0.95f, 0.20f, 0.25f, 1f),
            new ColorRGBA(0.98f, 0.55f, 0.10f, 1f),
            new ColorRGBA(0.95f, 0.90f, 0.20f, 1f),
            new ColorRGBA(0.25f, 0.90f, 0.35f, 1f),
            new ColorRGBA(0.20f, 0.80f, 0.85f, 1f),
            new ColorRGBA(0.30f, 0.45f, 0.95f, 1f),
            new ColorRGBA(0.65f, 0.30f, 0.95f, 1f),
            new ColorRGBA(0.95f, 0.35f, 0.80f, 1f),
    };

    private final EntityData ed;
    private final EntityId playerId;

    private Node sceneRoot;
    private Node guiNode;
    private Application app;
    private EntitySet blocks;
    private final Map<EntityId, BlockType> trackedTypes = new HashMap<>();

    private float poison;
    private float recolorTimer;
    private boolean recolored;

    private Geometry barBg;
    private Geometry barFill;
    private BitmapText barLabel;
    private AudioNode poisonSound;

    public PoisonState(EntityData ed, EntityId playerId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;
        this.sceneRoot = ((SimpleApplication) app).getRootNode();
        this.guiNode = ((SimpleApplication) app).getGuiNode();
        this.blocks = ed.getEntities(BlockComponent.class);
        this.blocks.applyChanges();
        for (Entity e : blocks) {
            trackedTypes.put(e.getId(), e.get(BlockComponent.class).blockType());
        }
        buildBar();

        try {
            poisonSound = new AudioNode(app.getAssetManager(), "Sound/poison.wav", AudioData.DataType.Buffer);
            poisonSound.setPositional(false);
            poisonSound.setVolume(0.8f);
        } catch (RuntimeException e) {
            poisonSound = null;
            System.err.println("[audio] poison sound unavailable (Sound/poison.wav): " + e.getMessage());
        }
    }

    @Override
    protected void cleanup(Application app) {
        if (recolored) {
            restoreRealBlocks();
            recolored = false;
        }
        hideBar();
        blocks.release();
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
        if (recolored) {
            restoreRealBlocks();
            recolored = false;
        }
        hideBar();
    }

    @Override
    public void update(float tpf) {
        blocks.applyChanges();

        boolean droning = droneEquipped();
        for (Entity removed : blocks.getRemovedEntities()) {
            BlockType type = trackedTypes.remove(removed.getId());
            if (type == BlockType.JELLYFISH && droning) {
                poison = Math.min(POISON_MAX, poison + POISON_PER_KILL);
                if (poisonSound != null) {
                    poisonSound.playInstance();
                }
            }
        }
        for (Entity added : blocks.getAddedEntities()) {
            trackedTypes.put(added.getId(), added.get(BlockComponent.class).blockType());
        }

        if (poison > 0f) {
            poison = Math.max(0f, poison - tpf);
        }

        if (poison > 0f) {
            recolorTimer += tpf;
            if (!recolored || recolorTimer >= RECOLOR_INTERVAL) {
                recolorTimer = 0f;
                recolorRealBlocks();
                recolored = true;
            }
            showBar(poison / POISON_MAX);
        } else if (recolored) {
            restoreRealBlocks();
            recolored = false;
            hideBar();
        } else {
            hideBar();
        }
    }

    private boolean droneEquipped() {
        WeaponComponent weapon = ed.getComponent(playerId, WeaponComponent.class);
        return weapon != null && weapon.weaponType() == WeaponType.DRONE;
    }

    private void recolorRealBlocks() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (Spatial child : sceneRoot.getChildren()) {
            if (child.getUserData(ModelViewSynchronizer.ENTITY_ID_USER_DATA) != null
                    && child instanceof Geometry geom) {
                ColorRGBA c = RAINBOW[rng.nextInt(RAINBOW.length)];
                applyColor(geom, c);
            }
        }
    }

    private void restoreRealBlocks() {
        for (Spatial child : sceneRoot.getChildren()) {
            if (child.getUserData(ModelViewSynchronizer.ENTITY_ID_USER_DATA) != null
                    && child instanceof Geometry geom) {
                // Restore each block's recorded base Diffuse (White for textured blocks so the texture
                // shows true colour, the flat per-type colour otherwise).
                ColorRGBA base = geom.getUserData(ModelViewState.BASE_DIFFUSE_USER_DATA);
                applyColor(geom, base != null ? base : ModelViewState.colorFor(geom.getName()));
            }
        }
    }

    private void applyColor(Geometry geom, ColorRGBA color) {
        Material mat = geom.getMaterial();
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
    }

    private void buildBar() {
        Material bgMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0.55f));
        bgMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barBg = new Geometry("poison-bar-bg", new Quad(BAR_WIDTH, BAR_HEIGHT));
        barBg.setMaterial(bgMat);

        Material fillMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        fillMat.setColor("Color", new ColorRGBA(0.45f, 0.95f, 0.30f, 0.9f)); // toxic green
        fillMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barFill = new Geometry("poison-bar-fill", new Quad(BAR_WIDTH, BAR_HEIGHT));
        barFill.setMaterial(fillMat);

        // Bottom-centre, clear of the round/countdown/buildings HUD along the top edge.
        // A Quad grows upward from its origin, so the bar occupies [BAR_MARGIN, BAR_MARGIN+BAR_HEIGHT].
        float x = (app.getCamera().getWidth() - BAR_WIDTH) / 2f;
        barBg.setLocalTranslation(x, BAR_MARGIN, 0f);
        barFill.setLocalTranslation(x, BAR_MARGIN, 1f);

        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        barLabel = new BitmapText(font);
        barLabel.setText("POISON");
        barLabel.setSize(16f);
        barLabel.setColor(new ColorRGBA(0.6f, 1f, 0.5f, 1f));
        // BitmapText hangs down from its origin, so place its top a line-height above the bar.
        barLabel.setLocalTranslation(x, BAR_MARGIN + BAR_HEIGHT + barLabel.getLineHeight() + 4f, 1f);
    }

    private void showBar(float ratio) {
        if (barBg.getParent() == null) {
            guiNode.attachChild(barBg);
            guiNode.attachChild(barFill);
            guiNode.attachChild(barLabel);
        }
        barFill.setLocalScale(Math.max(0f, Math.min(1f, ratio)), 1f, 1f);
    }

    private void hideBar() {
        if (barBg != null && barBg.getParent() != null) {
            barBg.removeFromParent();
            barFill.removeFromParent();
            barLabel.removeFromParent();
        }
    }
}
