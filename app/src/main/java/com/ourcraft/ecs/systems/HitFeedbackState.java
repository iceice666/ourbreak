package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.ourcraft.ecs.components.BlockComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Hit juice: when a block's durability drops, flash its spatial white and punch its scale briefly so
 * the player gets clear feedback that an attack landed (otherwise a non-lethal hit looks like nothing
 * happened). Reads block durability from the ECS and animates the block spatials created by the
 * model-view synchronizer (resolved via their {@code entityId} user-data tag).
 */
public class HitFeedbackState extends BaseAppState {

    private static final float FLASH_DURATION = 0.14f;
    private static final float PUNCH_SCALE = 0.25f;

    private final EntityData ed;

    private static final String HIT_SOUND = "Sound/block-hit.wav";

    private Node sceneRoot;
    private EntitySet blocks;
    private AudioNode hitSound;

    private final Map<EntityId, Geometry> geometries = new HashMap<>();
    private final Map<EntityId, ColorRGBA> baseColors = new HashMap<>();
    private final Map<EntityId, Float> lastDurability = new HashMap<>();
    private final Map<EntityId, Float> flashRemaining = new HashMap<>();

    public HitFeedbackState(EntityData ed) {
        this.ed = Objects.requireNonNull(ed, "ed");
    }

    @Override
    protected void initialize(Application app) {
        this.sceneRoot = ((SimpleApplication) app).getRootNode();
        this.blocks = ed.getEntities(BlockComponent.class);

        try {
            hitSound = new AudioNode(app.getAssetManager(), HIT_SOUND, AudioData.DataType.Buffer);
            hitSound.setPositional(false);
            hitSound.setVolume(1.0f);
        } catch (RuntimeException e) {
            hitSound = null;
            System.err.println("[audio] hit sound unavailable (" + HIT_SOUND + "): " + e.getMessage());
        }
    }

    @Override
    protected void cleanup(Application app) {
        blocks.release();
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        blocks.applyChanges();

        for (Entity entity : blocks) {
            EntityId id = entity.getId();
            resolveGeometry(id);
            float durability = entity.get(BlockComponent.class).durability();
            Float previous = lastDurability.get(id);
            if (previous != null && durability < previous) {
                flashRemaining.put(id, FLASH_DURATION);
                if (hitSound != null) {
                    hitSound.playInstance();
                }
            }
            lastDurability.put(id, durability);
        }

        // Drop tracking for blocks that are gone.
        geometries.keySet().removeIf(id -> blocks.getEntity(id) == null);
        lastDurability.keySet().removeIf(id -> blocks.getEntity(id) == null);
        baseColors.keySet().removeIf(id -> blocks.getEntity(id) == null);

        animateFlashes(tpf);
    }

    private void resolveGeometry(EntityId id) {
        if (geometries.containsKey(id)) {
            return;
        }
        for (Spatial child : sceneRoot.getChildren()) {
            Long raw = child.getUserData(ModelViewSynchronizer.ENTITY_ID_USER_DATA);
            if (raw != null && raw == id.getId() && child instanceof Geometry geom) {
                geometries.put(id, geom);
                ColorRGBA base = (ColorRGBA) geom.getMaterial().getParam("Diffuse").getValue();
                baseColors.put(id, base.clone());
                return;
            }
        }
    }

    private void animateFlashes(float tpf) {
        flashRemaining.entrySet().removeIf(entry -> {
            EntityId id = entry.getKey();
            Geometry geom = geometries.get(id);
            ColorRGBA base = baseColors.get(id);
            if (geom == null || base == null) {
                return true;
            }

            float remaining = entry.getValue() - tpf;
            if (remaining <= 0f) {
                applyColor(geom, base);
                geom.setLocalScale(1f);
                return true;
            }

            float t = remaining / FLASH_DURATION; // 1 at the hit, fading to 0
            applyColor(geom, base.clone().interpolateLocal(ColorRGBA.White, t));
            geom.setLocalScale(1f + PUNCH_SCALE * t);
            entry.setValue(remaining);
            return false;
        });
    }

    private void applyColor(Geometry geom, ColorRGBA color) {
        Material mat = geom.getMaterial();
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
    }
}
