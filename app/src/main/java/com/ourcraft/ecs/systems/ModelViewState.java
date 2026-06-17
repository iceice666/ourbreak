package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.ourcraft.ecs.components.ModelComponent;
import com.simsilica.es.EntityData;

public class ModelViewState extends BaseAppState {

    /** Geometry user-data key for a block's base Diffuse colour, so hit-flash / poison can restore it. */
    public static final String BASE_DIFFUSE_USER_DATA = "baseDiffuse";

    private final EntityData entityData;
    private final Node sceneRoot;

    private AssetManager assetManager;
    private ModelViewSynchronizer synchronizer;

    public ModelViewState(EntityData entityData, Node sceneRoot) {
        this.entityData = entityData;
        this.sceneRoot = sceneRoot;
    }

    @Override
    protected void initialize(Application app) {
        this.assetManager = app.getAssetManager();
        this.synchronizer = new ModelViewSynchronizer(entityData, sceneRoot, this::createSpatial);
    }

    @Override
    protected void cleanup(Application app) {
        if (synchronizer != null) {
            synchronizer.cleanup();
            synchronizer = null;
        }
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        if (synchronizer != null) {
            synchronizer.synchronize();
        }
    }

    private Spatial createSpatial(ModelComponent model) {
        Geometry geom = new Geometry(model.modelId(), new Box(0.5f, 0.5f, 0.5f));
        Material mat = blockMaterial(assetManager, model.modelId());
        geom.setMaterial(mat);
        // Record the base Diffuse so hit-flash / poison can restore it (White for textured blocks).
        geom.setUserData(BASE_DIFFUSE_USER_DATA, ((ColorRGBA) mat.getParam("Diffuse").getValue()).clone());
        return geom;
    }

    /**
     * The Lighting material for a block model id, shared by the live blocks and the destruction debris.
     * Prefers the per-type seamless texture (Textures/&lt;type&gt;.png); falls back to a flat per-type
     * colour when no texture ships. With UseMaterialColors + a DiffuseMap, a White Diffuse base shows the
     * texture at true colour while hit-flash / poison can still tint by overwriting the Diffuse colour.
     */
    public static Material blockMaterial(AssetManager assetManager, String modelId) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        Texture texture = loadBlockTexture(assetManager, modelId);
        ColorRGBA base;
        if (texture != null) {
            texture.setWrap(Texture.WrapMode.Repeat);
            mat.setTexture("DiffuseMap", texture);
            base = ColorRGBA.White.clone();
        } else {
            base = colorFor(modelId);
        }
        mat.setColor("Diffuse", base);
        mat.setColor("Ambient", base);
        return mat;
    }

    private static Texture loadBlockTexture(AssetManager assetManager, String modelId) {
        String type = modelId.endsWith("-block")
                ? modelId.substring(0, modelId.length() - "-block".length())
                : modelId;
        try {
            return assetManager.loadTexture("Textures/" + type + ".png");
        } catch (RuntimeException e) {
            return null; // no texture for this type yet → caller falls back to a flat colour
        }
    }

    /** The per-type placeholder colour for a block model id (used to restore colours after the poison hallucination). */
    public static ColorRGBA colorFor(String modelId) {
        return switch (modelId) {
            case "sand-block" -> new ColorRGBA(0.85f, 0.78f, 0.45f, 1f);      // sandy tan
            case "coral-block" -> new ColorRGBA(0.95f, 0.42f, 0.6f, 1f);      // coral pink
            case "shell-block" -> new ColorRGBA(0.95f, 0.92f, 0.85f, 1f);     // shell cream
            case "rock-block" -> new ColorRGBA(0.38f, 0.4f, 0.43f, 1f);       // slate grey
            case "jellyfish-block" -> new ColorRGBA(0.45f, 0.5f, 0.95f, 1f);  // jelly blue
            default -> ColorRGBA.Cyan;
        };
    }
}
