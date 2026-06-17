package com.ourbreak.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.ourbreak.ecs.components.RoundComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

import java.util.Objects;

/**
 * Between-round flourish: when the player clears a village and the round advances, a banner drops in
 * for a couple of seconds — "ROUND N CLEARED / Next: Round N+1 · Drone Lv.X" — so a cleared round is
 * acknowledged and the next one doesn't start without warning. Purely visual (the next village rebuilds
 * behind it); it never blocks input.
 */
public class RoundBannerState extends BaseAppState {

    private static final float DURATION = 2.5f;

    private final EntityData ed;
    private final EntityId gameStateId;

    private Node guiNode;
    private Camera camera;
    private Container banner;
    private Label title;
    private Label subtitle;

    private int shownRound;
    private float timer;
    private boolean visible;

    public RoundBannerState(EntityData ed, EntityId gameStateId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
    }

    @Override
    protected void initialize(Application app) {
        this.guiNode = ((SimpleApplication) app).getGuiNode();
        this.camera = app.getCamera();

        banner = new Container();
        banner.setBackground(UiTheme.card(UiTheme.PANEL, 36f, 20f));
        title = banner.addChild(UiTheme.heading("", 40f, UiTheme.GOLD));
        subtitle = banner.addChild(UiTheme.heading("", 20f, UiTheme.AQUA));

        // Don't announce the very first round (nothing was "cleared" yet).
        RoundComponent round = ed.getComponent(gameStateId, RoundComponent.class);
        shownRound = round != null ? round.currentRound() : 1;
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
        if (visible) {
            banner.removeFromParent();
            visible = false;
        }
    }

    @Override
    public void update(float tpf) {
        RoundComponent round = ed.getComponent(gameStateId, RoundComponent.class);
        if (round == null) {
            return;
        }

        if (round.currentRound() > shownRound) {
            show(round.currentRound());
            shownRound = round.currentRound();
        }

        if (visible) {
            timer -= tpf;
            if (timer <= 0f) {
                banner.removeFromParent();
                visible = false;
            }
        }
    }

    private void show(int nextRound) {
        title.setText("ROUND " + (nextRound - 1) + " CLEARED");
        subtitle.setText("Next: Round " + nextRound);

        // Centred horizontally, in the upper third so it doesn't cover the rebuilding village.
        float w = banner.getPreferredSize().x;
        banner.setLocalTranslation((camera.getWidth() - w) / 2f, camera.getHeight() * 0.86f, 5f);

        if (!visible) {
            guiNode.attachChild(banner);
            visible = true;
        }
        timer = DURATION;
    }
}
