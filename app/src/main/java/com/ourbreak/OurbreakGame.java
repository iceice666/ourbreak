package com.ourbreak;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.ourbreak.ecs.systems.AudioState;
import com.ourbreak.ecs.systems.MainMenuState;
import com.simsilica.lemur.GuiGlobals;

public class OurbreakGame extends SimpleApplication {

    private static final float SUN_DISTANCE = 900f;
    /** Direction from the world toward the sun (up, slightly to the side, toward the play area front). */
    private final Vector3f sunDir = new Vector3f(0.28f, 0.8f, -0.52f).normalizeLocal();

    private Node sunNode;
    private LightScatteringFilter sunScatter;

    public static void main(String[] args) {
        new OurbreakGame().start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        // Hide jME's debug HUD (FPS + render stats) — it overlaps menus and isn't for players.
        setDisplayStatView(false);
        setDisplayFps(false);
        // WSLg hides the OS cursor as soon as it enters the window unless we explicitly keep it
        // visible; without this the Lemur menu buttons are unclickable (no pointer to click with).
        inputManager.setCursorVisible(true);

        // Lemur GUI bootstrap (default Java styling — no Groovy glass style).
        GuiGlobals.initialize(this);

        // Sunny beach sky.
        viewPort.setBackgroundColor(new ColorRGBA(0.53f, 0.81f, 0.92f, 1f));

        // Warm afternoon sun + warm ambient fill so the sand reads as a sunny beach, not a cold cave.
        // The light travels opposite the visible sun, so shadows line up with where the sun disc sits.
        DirectionalLight sun = new DirectionalLight(sunDir.negate());
        sun.setColor(new ColorRGBA(1f, 0.96f, 0.84f, 1f).mult(1.1f));
        rootNode.addLight(sun);
        rootNode.addLight(new AmbientLight(new ColorRGBA(0.62f, 0.6f, 0.52f, 1f)));

        setUpLightingAndPostFx(sun);

        stateManager.attach(new AudioState());
        stateManager.attach(new MainMenuState());
    }

    /** Fidelity pass (no gameplay change): a real sky + sun + god rays, soft shadows, bloom, vignette. */
    private void setUpLightingAndPostFx(DirectionalLight sun) {
        // Gradient sky dome (zenith → horizon → ground) instead of a flat background colour.
        rootNode.attachChild(SkyFactory.createSky(
                assetManager, assetManager.loadTexture("Textures/sky.png"), SkyFactory.EnvMapType.EquirectMap));

        // The visible sun: a billboarded, overbright glowing disc far along the sun direction (driven into
        // bloom). Centred in a node so the billboard rotates about the disc's middle.
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setTexture("ColorMap", assetManager.loadTexture("Textures/sun.png"));
        sunMat.setColor("Color", new ColorRGBA(1.5f, 1.4f, 1.15f, 1f)); // > 1 so it blooms hard
        sunMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
        sunMat.getAdditionalRenderState().setDepthWrite(false);
        float sunSize = 110f;
        Geometry sunQuad = new Geometry("sun-quad", new Quad(sunSize, sunSize));
        sunQuad.setMaterial(sunMat);
        sunQuad.setLocalTranslation(-sunSize / 2f, -sunSize / 2f, 0f);
        sunQuad.setQueueBucket(RenderQueue.Bucket.Transparent);
        sunQuad.setShadowMode(RenderQueue.ShadowMode.Off);
        sunNode = new Node("sun");
        sunNode.attachChild(sunQuad);
        sunNode.addControl(new BillboardControl());
        rootNode.attachChild(sunNode);

        // Houses / crab / debris cast and receive shadows on the sand → a real place, not floating blocks.
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        DirectionalLightShadowRenderer shadows = new DirectionalLightShadowRenderer(assetManager, 2048, 3);
        shadows.setLight(sun);
        shadows.setShadowIntensity(0.4f);            // soft, sunny — not a harsh black
        shadows.setEdgeFilteringMode(EdgeFilteringMode.PCF4);
        viewPort.addProcessor(shadows);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        // God rays: volumetric light shafts streaming from the sun (the UE-ish money shot).
        sunScatter = new LightScatteringFilter(sunWorldPosition());
        sunScatter.setLightDensity(0.9f);
        fpp.addFilter(sunScatter);
        // Bloom: the sun, sun-lit sand and the drone fireballs glow.
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Scene);
        bloom.setBloomIntensity(1.15f);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);

        // Vignette: a radial darkening overlay on the GUI to draw the eye to the centre.
        Texture vig = assetManager.loadTexture("Textures/vignette.png");
        Material vigMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        vigMat.setTexture("ColorMap", vig);
        vigMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        Geometry vignette = new Geometry("vignette", new Quad(cam.getWidth(), cam.getHeight()));
        vignette.setMaterial(vigMat);
        vignette.setQueueBucket(RenderQueue.Bucket.Transparent);
        vignette.setLocalTranslation(0f, 0f, -1f); // behind the HUD/menus, over the 3D scene
        guiNode.attachChild(vignette);
    }

    private Vector3f sunWorldPosition() {
        return cam.getLocation().add(sunDir.mult(SUN_DISTANCE));
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Keep the sun "at infinity" in its fixed direction as the camera moves, and feed the god rays.
        if (sunNode != null) {
            Vector3f pos = sunWorldPosition();
            sunNode.setLocalTranslation(pos);
            if (sunScatter != null) {
                sunScatter.setLightPosition(pos);
            }
        }
    }
}
