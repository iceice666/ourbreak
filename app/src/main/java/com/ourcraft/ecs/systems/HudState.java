package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.RoundComponent;
import com.ourcraft.ecs.components.WeaponComponent;
import com.ourcraft.ecs.components.WeaponComponent.WeaponType;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.lemur.Label;

import java.util.Locale;
import java.util.Objects;

/**
 * In-gameplay HUD: round counter (always), attack countdown and remaining-building count (ATTACK
 * phase only). Reads ECS state each frame and renders it via Lemur labels; all string formatting is
 * delegated to the pure {@link HudText} helper.
 */
public class HudState extends BaseAppState {

    private static final float MARGIN = 12f;
    private static final float FONT_SIZE = 24f;
    private static final float WEAPON_FONT_SIZE = 34f;
    private static final float ICON_SIZE = 110f;

    private final EntityData ed;
    private final EntityId gameStateId;
    private final EntityId playerId;

    private AssetManager assetManager;
    private Node guiNode;
    private Camera camera;
    private EntitySet blocks;

    private Label roundLabel;
    private Label countdownLabel;
    private Label buildingsLabel;
    private Label weaponLabel;
    private Picture weaponIcon;
    private WeaponType shownWeapon;
    private float iconW;
    private float iconH;

    public HudState(EntityData ed, EntityId gameStateId, EntityId playerId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    @Override
    protected void initialize(Application app) {
        this.assetManager = app.getAssetManager();
        this.guiNode = ((SimpleApplication) app).getGuiNode();
        this.camera = app.getCamera();
        this.blocks = ed.getEntities(BlockComponent.class);

        roundLabel = label();
        roundLabel.setColor(UiTheme.GOLD);
        countdownLabel = label();
        buildingsLabel = label();
        buildingsLabel.setColor(UiTheme.AQUA);
        weaponLabel = label();
        weaponLabel.setColor(UiTheme.GOLD);
        weaponLabel.setFontSize(WEAPON_FONT_SIZE); // weapon readout is enlarged vs the rest of the HUD

        weaponIcon = new Picture("weapon-icon");
        weaponIcon.setWidth(ICON_SIZE);
        weaponIcon.setHeight(ICON_SIZE);
        weaponIcon.setPosition(MARGIN, MARGIN);
    }

    @Override
    protected void cleanup(Application app) {
        blocks.release();
    }

    @Override
    protected void onEnable() {
        // countdown + buildings are attached on demand during ATTACK (see update) so they don't leave
        // empty pills during BUILD.
        guiNode.attachChild(roundLabel);
        guiNode.attachChild(weaponLabel);
        guiNode.attachChild(weaponIcon);
    }

    @Override
    protected void onDisable() {
        roundLabel.removeFromParent();
        countdownLabel.removeFromParent();
        buildingsLabel.removeFromParent();
        weaponLabel.removeFromParent();
        weaponIcon.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        RoundComponent round = ed.getComponent(gameStateId, RoundComponent.class);
        PhaseComponent phase = ed.getComponent(gameStateId, PhaseComponent.class);
        if (round == null || phase == null) {
            return;
        }

        float top = camera.getHeight() - MARGIN;
        roundLabel.setText(HudText.round(round.currentRound()));
        roundLabel.setLocalTranslation(MARGIN, top, 0f);

        boolean attacking = phase.phase() == Phase.ATTACK;
        if (attacking) {
            if (countdownLabel.getParent() == null) {
                guiNode.attachChild(countdownLabel);
                guiNode.attachChild(buildingsLabel);
            }
            countdownLabel.setText(HudText.countdown(round.remainingSeconds()));
            // Urgency: the clock turns coral-red in the final 10 seconds.
            countdownLabel.setColor(round.remainingSeconds() <= 10.0 ? UiTheme.CORAL : UiTheme.GOLD);
            countdownLabel.setLocalTranslation(
                    camera.getWidth() - MARGIN - countdownLabel.getPreferredSize().x, top, 0f);

            blocks.applyChanges();
            buildingsLabel.setText(HudText.buildings(blocks.size()));
            buildingsLabel.setLocalTranslation(
                    (camera.getWidth() - buildingsLabel.getPreferredSize().x) / 2f, top, 0f);
        } else if (countdownLabel.getParent() != null) {
            countdownLabel.removeFromParent();
            buildingsLabel.removeFromParent();
        }

        WeaponComponent weapon = ed.getComponent(playerId, WeaponComponent.class);
        if (weapon != null) {
            WeaponType type = weapon.weaponType();
            if (type != shownWeapon) {
                // Swap the icon only when the equipped weapon changes (setImage reloads the texture).
                String path = "Icons/" + type.name().toLowerCase(Locale.ROOT) + ".png";
                Texture tex = assetManager.loadTexture(path);
                int iw = tex.getImage().getWidth();
                int ih = tex.getImage().getHeight();
                // Fit the source into the ICON_SIZE box preserving aspect ratio.
                float scale = ICON_SIZE / Math.max(iw, ih);
                iconW = iw * scale;
                iconH = ih * scale;
                weaponIcon.setImage(assetManager, path, true);
                weaponIcon.setWidth(iconW);
                weaponIcon.setHeight(iconH);
                shownWeapon = type;
            }

            // Layout: "Weapon: NAME" text at the bottom-left, then the icon right after it.
            weaponLabel.setText(HudText.weapon(type));
            float textH = weaponLabel.getPreferredSize().y;
            float textW = weaponLabel.getPreferredSize().x;
            weaponLabel.setLocalTranslation(MARGIN, MARGIN + textH, 0f);
            // Vertically centre the icon on the text line.
            weaponIcon.setPosition(MARGIN + textW + 8f, MARGIN + textH / 2f - iconH / 2f);
        }
    }

    private Label label() {
        Label label = new Label("");
        label.setColor(UiTheme.TEXT);
        label.setFontSize(FONT_SIZE);
        label.setBackground(UiTheme.pill()); // dark backing so HUD text reads over the bright beach
        return label;
    }
}
