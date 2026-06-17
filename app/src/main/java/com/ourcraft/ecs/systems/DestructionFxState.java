package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Destruction juice: when a block is destroyed it bursts into chunky debris (coloured like the block)
 * that flies out, tumbles, falls under gravity, bounces off the sand and fades, plus a quick dust puff.
 * Gives every break weight and a satisfying crumble instead of the block just vanishing. Purely visual
 * (jME geometries animated here, not ECS entities); capped so big AoE clears stay cheap.
 */
public class DestructionFxState extends BaseAppState {

    private static final int MAX_DEBRIS = 300;
    private static final int CHUNKS_PER_BLOCK = 7;
    private static final int DUST_PER_BLOCK = 3;
    private static final float GRAVITY = -11f;
    private static final float GROUND_Y = -0.5f;
    private static final float DEBRIS_LIFE = 1.1f;
    private static final float DEBRIS_FADE = 0.35f; // shrink over the last seconds
    private static final float DUST_LIFE = 0.45f;

    private final EntityData ed;

    private Node sceneRoot;
    private AssetManager assetManager;
    private Node fxNode;
    private EntitySet blocks;

    private final Mesh cubeMesh = new Box(0.5f, 0.5f, 0.5f);
    private final Mesh dustMesh = new Sphere(8, 8, 0.5f);
    private final Mesh ringMesh = new Torus(28, 10, 0.05f, 0.5f);

    private final Map<EntityId, BlockInfo> tracked = new HashMap<>();
    private final Map<String, Material> debrisMaterials = new HashMap<>();
    private final List<Debris> debris = new ArrayList<>();
    private final List<Dust> dust = new ArrayList<>();
    private final List<Puff> puffs = new ArrayList<>();
    private final List<FxLight> lights = new ArrayList<>();

    public DestructionFxState(EntityData ed) {
        this.ed = Objects.requireNonNull(ed, "ed");
    }

    @Override
    protected void initialize(Application app) {
        this.sceneRoot = ((SimpleApplication) app).getRootNode();
        this.assetManager = app.getAssetManager();
        this.blocks = ed.getEntities(BlockComponent.class, PositionComponent.class, ModelComponent.class);
        this.fxNode = new Node("destruction-fx");
    }

    @Override
    protected void cleanup(Application app) {
        blocks.release();
    }

    @Override
    protected void onEnable() {
        sceneRoot.attachChild(fxNode);
    }

    @Override
    protected void onDisable() {
        fxNode.detachAllChildren();
        debris.clear();
        dust.clear();
        puffs.clear();
        for (FxLight l : lights) {
            sceneRoot.removeLight(l.light);
        }
        lights.clear();
        fxNode.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        blocks.applyChanges();

        // Remember each live block's position + model so we know what shattered when it's removed.
        for (Entity block : blocks) {
            PositionComponent p = block.get(PositionComponent.class);
            tracked.put(block.getId(), new BlockInfo(p.x(), p.y(), p.z(), block.get(ModelComponent.class).modelId()));
        }

        for (Entity removed : blocks.getRemovedEntities()) {
            BlockInfo info = tracked.remove(removed.getId());
            if (info != null && debris.size() < MAX_DEBRIS) {
                burst(info);
            }
        }

        animateDebris(tpf);
        animateDust(tpf);
        animatePuffs(tpf);
        animateLights(tpf);
    }

    private void burst(BlockInfo info) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Material mat = debrisMaterial(info.modelId());

        for (int i = 0; i < CHUNKS_PER_BLOCK; i++) {
            float size = 0.12f + rng.nextFloat() * 0.12f;
            Geometry g = new Geometry("debris", cubeMesh);
            g.setMaterial(mat);
            g.setLocalScale(size);
            Vector3f pos = new Vector3f(
                    info.x() + rng.nextFloat(-0.3f, 0.3f),
                    info.y() + rng.nextFloat(-0.2f, 0.3f),
                    info.z() + rng.nextFloat(-0.3f, 0.3f));
            g.setLocalTranslation(pos);
            fxNode.attachChild(g);

            float ang = rng.nextFloat() * FastMath.TWO_PI;
            float speed = 1.8f + rng.nextFloat() * 2.8f;
            Vector3f vel = new Vector3f(
                    FastMath.cos(ang) * speed,
                    2.2f + rng.nextFloat() * 3.4f,
                    FastMath.sin(ang) * speed);
            Vector3f spin = new Vector3f(
                    rng.nextFloat(-9f, 9f), rng.nextFloat(-9f, 9f), rng.nextFloat(-9f, 9f));
            debris.add(new Debris(g, pos, vel, spin, size, DEBRIS_LIFE));
        }

        for (int i = 0; i < DUST_PER_BLOCK; i++) {
            Material dustMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            dustMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            Geometry g = new Geometry("dust", dustMesh);
            g.setMaterial(dustMat);
            g.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
            g.setLocalTranslation(
                    info.x() + rng.nextFloat(-0.25f, 0.25f),
                    info.y() + rng.nextFloat(-0.1f, 0.25f),
                    info.z() + rng.nextFloat(-0.25f, 0.25f));
            fxNode.attachChild(g);
            dust.add(new Dust(g, dustMat, DUST_LIFE, 0.25f + rng.nextFloat() * 0.25f));
        }
    }

    private void animateDebris(float tpf) {
        for (Iterator<Debris> it = debris.iterator(); it.hasNext(); ) {
            Debris d = it.next();
            d.life -= tpf;
            if (d.life <= 0f) {
                d.geom.removeFromParent();
                it.remove();
                continue;
            }
            d.vel.y += GRAVITY * tpf;
            d.pos.addLocal(d.vel.x * tpf, d.vel.y * tpf, d.vel.z * tpf);
            float rest = GROUND_Y + d.size * 0.5f;
            if (d.pos.y < rest) {
                d.pos.y = rest;
                d.vel.y *= -0.35f;       // bounce
                d.vel.x *= 0.6f;          // ground friction
                d.vel.z *= 0.6f;
            }
            d.geom.setLocalTranslation(d.pos);
            d.geom.rotate(d.spin.x * tpf, d.spin.y * tpf, d.spin.z * tpf);
            float fade = d.life < DEBRIS_FADE ? d.life / DEBRIS_FADE : 1f;
            d.geom.setLocalScale(d.size * fade);
        }
    }

    private void animateDust(float tpf) {
        for (Iterator<Dust> it = dust.iterator(); it.hasNext(); ) {
            Dust d = it.next();
            d.life -= tpf;
            if (d.life <= 0f) {
                d.geom.removeFromParent();
                it.remove();
                continue;
            }
            float t = d.life / DUST_LIFE;            // 1 → 0
            float scale = d.maxScale * (1.1f - t);   // expand as it fades
            d.geom.setLocalScale(scale);
            d.material.setColor("Color", new ColorRGBA(0.86f, 0.80f, 0.70f, 0.45f * t));
        }
    }

    private Material debrisMaterial(String modelId) {
        // Same textured material the live blocks use, so chunks carry the block's real texture.
        return debrisMaterials.computeIfAbsent(modelId, id -> ModelViewState.blockMaterial(assetManager, id));
    }

    /**
     * A drone blast at a world position: a white core flash, an orange fireball, rising smoke, an
     * expanding ground shockwave ring, and a brief orange point light. (The 3×3 of blocks it destroys
     * already throw their own textured debris.)
     */
    public void explosion(Vector3f center) {
        explosion(center, 1);
    }

    /** Explosion sized to the drone blast radius (Lv1 ≈ 3×3 → s=1; larger levels scale the FX up). */
    public void explosion(Vector3f center, int blastRadius) {
        float s = (2 * Math.max(1, blastRadius) + 1) / 3f; // diameter in cells / 3 → 1 at Lv1

        // core flash + fireball (additive glow)
        spawnPuff(dustMesh, center, new ColorRGBA(1f, 0.97f, 0.85f, 1f), 0.95f, 0.6f * s, 1.7f * s, 0f, 0.14f, true, false);
        spawnPuff(dustMesh, center, new ColorRGBA(1f, 0.55f, 0.18f, 1f), 0.95f, 0.5f * s, 2.7f * s, 0.6f, 0.40f, true, false);
        spawnPuff(dustMesh, center, new ColorRGBA(1f, 0.82f, 0.35f, 1f), 0.9f, 0.3f * s, 1.7f * s, 0.5f, 0.30f, true, false);

        // rising smoke (alpha)
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < 5; i++) {
            Vector3f p = new Vector3f(
                    center.x + rng.nextFloat(-0.4f, 0.4f) * s,
                    center.y + rng.nextFloat(-0.2f, 0.3f),
                    center.z + rng.nextFloat(-0.4f, 0.4f) * s);
            spawnPuff(dustMesh, p, new ColorRGBA(0.17f, 0.16f, 0.15f, 1f), 0.7f, 0.5f * s, 2.0f * s, 1.4f, 0.85f, false, false);
        }

        // ground shockwave ring (additive)
        Vector3f ringPos = new Vector3f(center.x, GROUND_Y + 0.06f, center.z);
        spawnPuff(ringMesh, ringPos, new ColorRGBA(1f, 0.78f, 0.4f, 1f), 0.85f, 0.4f * s, 3.2f * s, 0f, 0.42f, true, true);

        // brief light
        PointLight pl = new PointLight();
        pl.setRadius(9f * s);
        pl.setPosition(center);
        sceneRoot.addLight(pl);
        lights.add(new FxLight(pl, 0.22f, new ColorRGBA(1f, 0.6f, 0.25f, 1f), 7f));
    }

    private void spawnPuff(Mesh mesh, Vector3f pos, ColorRGBA rgb, float startAlpha,
                           float startScale, float endScale, float rise, float life,
                           boolean additive, boolean flatRing) {
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", new ColorRGBA(rgb.r, rgb.g, rgb.b, startAlpha));
        m.getAdditionalRenderState().setBlendMode(
                additive ? RenderState.BlendMode.AlphaAdditive : RenderState.BlendMode.Alpha);
        m.getAdditionalRenderState().setDepthWrite(false);
        Geometry g = new Geometry("fx-puff", mesh);
        g.setMaterial(m);
        g.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        if (flatRing) {
            g.rotate(-FastMath.HALF_PI, 0f, 0f); // lay the ring flat on the ground
        }
        g.setLocalTranslation(pos);
        g.setLocalScale(startScale);
        fxNode.attachChild(g);
        puffs.add(new Puff(g, m, pos.clone(), rgb, startAlpha, startScale, endScale, rise, life));
    }

    private void animatePuffs(float tpf) {
        for (Iterator<Puff> it = puffs.iterator(); it.hasNext(); ) {
            Puff p = it.next();
            p.life -= tpf;
            if (p.life <= 0f) {
                p.geom.removeFromParent();
                it.remove();
                continue;
            }
            float t = 1f - p.life / p.maxLife;           // 0 → 1
            p.geom.setLocalScale(p.startScale + (p.endScale - p.startScale) * t);
            if (p.rise != 0f) {
                p.pos.y += p.rise * tpf;
                p.geom.setLocalTranslation(p.pos);
            }
            float alpha = p.startAlpha * (1f - t);
            p.material.setColor("Color", new ColorRGBA(p.rgb.r, p.rgb.g, p.rgb.b, alpha));
        }
    }

    private void animateLights(float tpf) {
        for (Iterator<FxLight> it = lights.iterator(); it.hasNext(); ) {
            FxLight l = it.next();
            l.life -= tpf;
            if (l.life <= 0f) {
                sceneRoot.removeLight(l.light);
                it.remove();
                continue;
            }
            float k = (l.life / l.maxLife) * l.intensity; // fade the brightness out
            l.light.setColor(l.color.mult(k));
        }
    }

    private record BlockInfo(float x, float y, float z, String modelId) {
    }

    private static final class Debris {
        final Geometry geom;
        final Vector3f pos;
        final Vector3f vel;
        final Vector3f spin;
        final float size;
        float life;

        Debris(Geometry geom, Vector3f pos, Vector3f vel, Vector3f spin, float size, float life) {
            this.geom = geom;
            this.pos = pos;
            this.vel = vel;
            this.spin = spin;
            this.size = size;
            this.life = life;
        }
    }

    private static final class Dust {
        final Geometry geom;
        final Material material;
        final float maxScale;
        float life;

        Dust(Geometry geom, Material material, float life, float maxScale) {
            this.geom = geom;
            this.material = material;
            this.life = life;
            this.maxScale = maxScale;
        }
    }

    /** A generic expanding / rising / fading explosion sprite (flash, fireball, smoke, shockwave ring). */
    private static final class Puff {
        final Geometry geom;
        final Material material;
        final Vector3f pos;
        final ColorRGBA rgb;
        final float startAlpha;
        final float startScale;
        final float endScale;
        final float rise;
        final float maxLife;
        float life;

        Puff(Geometry geom, Material material, Vector3f pos, ColorRGBA rgb, float startAlpha,
             float startScale, float endScale, float rise, float life) {
            this.geom = geom;
            this.material = material;
            this.pos = pos;
            this.rgb = rgb;
            this.startAlpha = startAlpha;
            this.startScale = startScale;
            this.endScale = endScale;
            this.rise = rise;
            this.maxLife = life;
            this.life = life;
        }
    }

    private static final class FxLight {
        final PointLight light;
        final ColorRGBA color;
        final float intensity;
        final float maxLife;
        float life;

        FxLight(PointLight light, float life, ColorRGBA color, float intensity) {
            this.light = light;
            this.color = color;
            this.intensity = intensity;
            this.maxLife = life;
            this.life = life;
        }
    }
}
