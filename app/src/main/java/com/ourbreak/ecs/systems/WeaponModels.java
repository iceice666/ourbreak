package com.ourbreak.ecs.systems;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

/**
 * Procedural low-poly 3D models of the three weapons, built from jME primitives to match their icon
 * art (steel-bladed sword, gold-and-maroon pistol, red quad-rotor drone). Used by {@link HeldWeaponState}
 * as a first-person held viewmodel. Each model is built already posed for the hand, with its rotor /
 * moving sub-node named so the viewmodel can animate it.
 */
final class WeaponModels {

    private static final ColorRGBA STEEL = new ColorRGBA(0.80f, 0.83f, 0.88f, 1f);
    private static final ColorRGBA STEEL_DK = new ColorRGBA(0.62f, 0.66f, 0.72f, 1f);
    private static final ColorRGBA GOLD = new ColorRGBA(0.95f, 0.74f, 0.22f, 1f);
    private static final ColorRGBA GOLD_DK = new ColorRGBA(0.80f, 0.58f, 0.15f, 1f);
    private static final ColorRGBA MAROON = new ColorRGBA(0.58f, 0.13f, 0.13f, 1f);
    private static final ColorRGBA MAROON_DK = new ColorRGBA(0.44f, 0.09f, 0.09f, 1f);
    private static final ColorRGBA DRONE_RED = new ColorRGBA(0.86f, 0.22f, 0.17f, 1f);
    private static final ColorRGBA DRONE_DK = new ColorRGBA(0.66f, 0.15f, 0.12f, 1f);
    private static final ColorRGBA LENS = new ColorRGBA(0.35f, 0.78f, 1f, 1f);
    private static final ColorRGBA GREY = new ColorRGBA(0.72f, 0.74f, 0.78f, 1f);
    private static final ColorRGBA DARK = new ColorRGBA(0.16f, 0.16f, 0.19f, 1f);
    private static final ColorRGBA WHITE = new ColorRGBA(0.98f, 0.98f, 1f, 1f);

    private WeaponModels() {
    }

    /** Classic sword: gold pommel, maroon grip, gold crossguard, tapered steel blade with a diamond tip. */
    static Node sword(AssetManager am) {
        Node n = new Node("weapon-sword");
        n.attachChild(part(am, "sword-pommel", new Sphere(16, 16, 0.05f), GOLD, 0f, 0f, 0f, 1f, 1f, 1f));
        n.attachChild(part(am, "sword-grip", new Box(0.028f, 0.075f, 0.028f), MAROON, 0f, 0.085f, 0f, 1f, 1f, 1f));
        n.attachChild(part(am, "sword-guard", new Box(0.14f, 0.028f, 0.045f), GOLD, 0f, 0.175f, 0f, 1f, 1f, 1f));
        n.attachChild(part(am, "sword-guard-end", new Sphere(10, 10, 0.035f), GOLD_DK, 0.135f, 0.175f, 0f, 1f, 1f, 1f));
        n.attachChild(part(am, "sword-guard-end2", new Sphere(10, 10, 0.035f), GOLD_DK, -0.135f, 0.175f, 0f, 1f, 1f, 1f));
        n.attachChild(part(am, "sword-blade", new Box(0.042f, 0.28f, 0.012f), STEEL, 0f, 0.48f, 0f, 1f, 1f, 1f));
        n.attachChild(part(am, "sword-fuller", new Box(0.008f, 0.26f, 0.014f), STEEL_DK, 0f, 0.48f, 0f, 1f, 1f, 1f));
        Geometry tip = part(am, "sword-tip", new Box(0.042f, 0.06f, 0.012f), STEEL, 0f, 0.78f, 0f, 1f, 1f, 1f);
        tip.rotate(0f, 0f, FastMath.QUARTER_PI); // diamond point above the blade
        n.attachChild(tip);
        // Posed for the hand: blade up, tip angled forward and slightly across the view.
        n.setLocalRotation(new Quaternion().fromAngles(-0.5f, 0.18f, 0.34f));
        n.setLocalScale(0.85f);
        return n;
    }

    /** Cartoon pistol: gold receiver + round barrel, maroon pump / grip / accents, trigger guard. */
    static Node gun(AssetManager am) {
        Node n = new Node("weapon-gun");
        n.attachChild(part(am, "gun-body", new Box(0.05f, 0.06f, 0.16f), GOLD, 0f, 0f, 0f, 1f, 1f, 1f));
        n.attachChild(part(am, "gun-barrel", new Cylinder(2, 14, 0.032f, 0.032f, 0.30f, true, false),
                GOLD, 0f, 0.02f, -0.20f, 1f, 1f, 1f));
        n.attachChild(part(am, "gun-barrel-top", new Box(0.03f, 0.022f, 0.14f), GOLD_DK, 0f, 0.055f, -0.16f, 1f, 1f, 1f));
        n.attachChild(part(am, "gun-muzzle", new Cylinder(2, 14, 0.037f, 0.037f, 0.03f, true, false),
                DARK, 0f, 0.02f, -0.35f, 1f, 1f, 1f));
        n.attachChild(part(am, "gun-pump", new Box(0.045f, 0.04f, 0.09f), MAROON, 0f, -0.055f, -0.11f, 1f, 1f, 1f));
        for (int s = -1; s <= 1; s += 2) {
            n.attachChild(part(am, "gun-accent", new Box(0.012f, 0.03f, 0.06f), MAROON, s * 0.056f, 0.012f, 0.02f, 1f, 1f, 1f));
        }
        Geometry grip = part(am, "gun-grip", new Box(0.04f, 0.095f, 0.045f), MAROON, 0f, -0.11f, 0.07f, 1f, 1f, 1f);
        grip.rotate(0.34f, 0f, 0f); // raked back like the icon
        n.attachChild(grip);
        n.attachChild(part(am, "gun-grip-cap", new Box(0.042f, 0.02f, 0.045f), MAROON_DK, 0f, -0.16f, 0.085f, 1f, 1f, 1f));
        n.attachChild(part(am, "gun-guard", new Box(0.012f, 0.022f, 0.045f), MAROON_DK, 0f, -0.05f, 0.005f, 1f, 1f, 1f));
        // Posed: barrel pointing forward and angled across the view.
        n.setLocalRotation(new Quaternion().fromAngles(0.06f, 0.36f, 0f));
        n.setLocalScale(0.92f);
        return n;
    }

    /** Quad-rotor drone: red body, glowing blue eye, four spinning rotors (sub-node "rotors"), landing legs. */
    static Node drone(AssetManager am) {
        Node n = new Node("weapon-drone");
        n.attachChild(part(am, "drone-body", new Sphere(20, 20, 0.14f), DRONE_RED, 0f, 0f, 0f, 1.05f, 0.92f, 1.05f));
        n.attachChild(part(am, "drone-band", new Sphere(20, 20, 0.142f), DRONE_DK, 0f, 0f, 0f, 1.05f, 0.45f, 1.05f));
        // Glowing camera eye on the front (-Z).
        n.attachChild(part(am, "drone-socket", new Sphere(14, 14, 0.062f), DARK, 0f, 0f, -0.105f, 1f, 1f, 0.7f));
        n.attachChild(part(am, "drone-lens", new Sphere(14, 14, 0.046f), LENS, 0f, 0f, -0.135f, 1f, 1f, 1f));
        n.attachChild(part(am, "drone-glint", new Sphere(8, 8, 0.016f), WHITE, 0.02f, 0.02f, -0.165f, 1f, 1f, 1f));

        // Four arms + rotors on a spinnable node.
        Node rotors = new Node("rotors");
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                float hx = dx * 0.17f, hz = dz * 0.11f;
                float yaw = FastMath.atan2(-hz, hx);
                Geometry arm = part(am, "drone-arm", new Box(0.10f, 0.012f, 0.014f), DRONE_DK,
                        hx * 0.5f, 0.035f, hz * 0.5f, 1f, 1f, 1f);
                arm.setLocalRotation(new Quaternion().fromAngles(0f, yaw, 0f));
                n.attachChild(arm);
                n.attachChild(part(am, "drone-hub", new Sphere(8, 8, 0.022f), DRONE_RED, hx, 0.06f, hz, 1f, 1f, 1f));
                Geometry rotor = part(am, "drone-rotor", new Cylinder(2, 16, 0.11f, 0.006f, true),
                        DARK, hx, 0.075f, hz, 1f, 1f, 1f);
                rotor.rotate(FastMath.HALF_PI, 0f, 0f); // lay the disc flat (spins about Y)
                rotors.attachChild(rotor);
            }
        }
        n.attachChild(rotors);

        // Two splayed landing legs at the bottom front.
        for (int s = -1; s <= 1; s += 2) {
            Geometry leg = part(am, "drone-leg", new Box(0.016f, 0.07f, 0.016f), GREY, s * 0.07f, -0.15f, -0.03f, 1f, 1f, 1f);
            leg.rotate(0.2f, 0f, s * 0.3f);
            n.attachChild(leg);
            n.attachChild(part(am, "drone-foot", new Box(0.05f, 0.012f, 0.016f), GREY, s * 0.10f, -0.19f, -0.03f, 1f, 1f, 1f));
        }
        // Posed: nosed slightly down so the player sees the top rotors hovering in the hand.
        n.setLocalRotation(new Quaternion().fromAngles(0.28f, 0f, 0f));
        n.setLocalScale(1f);
        return n;
    }

    private static Geometry part(AssetManager am, String name, Mesh mesh, ColorRGBA color,
                                 float x, float y, float z, float sx, float sy, float sz) {
        Geometry g = new Geometry(name, mesh);
        Material m = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse", color);
        m.setColor("Ambient", color);
        g.setMaterial(m);
        g.setLocalScale(sx, sy, sz);
        g.setLocalTranslation(x, y, z);
        return g;
    }
}
