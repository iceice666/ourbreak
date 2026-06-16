package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.RoundComponent;
import com.ourcraft.ecs.components.WeaponComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.lemur.Label;

import java.util.Objects;

/**
 * In-gameplay HUD: round counter (always), attack countdown and remaining-building count (ATTACK
 * phase only). Reads ECS state each frame and renders it via Lemur labels; all string formatting is
 * delegated to the pure {@link HudText} helper.
 */
public class HudState extends BaseAppState {

    private static final float MARGIN = 12f;
    private static final float FONT_SIZE = 24f;

    private final EntityData ed;
    private final EntityId gameStateId;
    private final EntityId playerId;

    private Node guiNode;
    private Camera camera;
    private EntitySet blocks;

    private Label roundLabel;
    private Label countdownLabel;
    private Label buildingsLabel;
    private Label weaponLabel;

    public HudState(EntityData ed, EntityId gameStateId, EntityId playerId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    @Override
    protected void initialize(Application app) {
        this.guiNode = ((SimpleApplication) app).getGuiNode();
        this.camera = app.getCamera();
        this.blocks = ed.getEntities(BlockComponent.class);

        roundLabel = label();
        countdownLabel = label();
        buildingsLabel = label();
        weaponLabel = label();
    }

    @Override
    protected void cleanup(Application app) {
        blocks.release();
    }

    @Override
    protected void onEnable() {
        guiNode.attachChild(roundLabel);
        guiNode.attachChild(countdownLabel);
        guiNode.attachChild(buildingsLabel);
        guiNode.attachChild(weaponLabel);
    }

    @Override
    protected void onDisable() {
        roundLabel.removeFromParent();
        countdownLabel.removeFromParent();
        buildingsLabel.removeFromParent();
        weaponLabel.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        RoundComponent round = ed.getComponent(gameStateId, RoundComponent.class);
        PhaseComponent phase = ed.getComponent(gameStateId, PhaseComponent.class);
        if (round == null || phase == null) {
            return;
        }

        float top = camera.getHeight() - MARGIN;
        roundLabel.setText(HudText.round(round.currentRound(), round.maxRounds()));
        roundLabel.setLocalTranslation(MARGIN, top, 0f);

        boolean attacking = phase.phase() == Phase.ATTACK;
        if (attacking) {
            countdownLabel.setText(HudText.countdown(round.remainingSeconds()));
            countdownLabel.setLocalTranslation(
                    camera.getWidth() - MARGIN - countdownLabel.getPreferredSize().x, top, 0f);

            blocks.applyChanges();
            buildingsLabel.setText(HudText.buildings(blocks.size()));
            buildingsLabel.setLocalTranslation(
                    (camera.getWidth() - buildingsLabel.getPreferredSize().x) / 2f, top, 0f);
        } else {
            countdownLabel.setText("");
            buildingsLabel.setText("");
        }

        WeaponComponent weapon = ed.getComponent(playerId, WeaponComponent.class);
        if (weapon != null) {
            weaponLabel.setText(HudText.weapon(weapon.weaponType()));
            weaponLabel.setLocalTranslation(MARGIN, MARGIN + weaponLabel.getPreferredSize().y, 0f);
        }
    }

    private Label label() {
        Label label = new Label("");
        label.setColor(ColorRGBA.White);
        label.setFontSize(FONT_SIZE);
        return label;
    }
}
