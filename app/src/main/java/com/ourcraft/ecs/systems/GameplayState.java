package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.MascotComponent;
import com.ourcraft.ecs.components.PlayerHealthComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.ourcraft.ecs.components.WeaponComponent;
import com.ourcraft.ecs.components.WeaponComponent.WeaponType;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;

/**
 * Root state of a single match. Owns a fresh {@link EntityData}, wires the M1-M3 headless systems
 * to the renderer and to player input, advances them each frame, and transitions to the end screen
 * once the result is decided.
 */
public class GameplayState extends BaseAppState {

    /** Player starting hit points (GDD §Mechanics tuning). */
    private static final float PLAYER_MAX_HEALTH = 100f;

    private EntityData ed;
    private ModelViewState modelView;
    private EnvironmentState environment;
    private RoundSystem roundSystem;
    private VictorySystem victorySystem;
    private NpcBuilderSystem npcBuilder;
    private BlockEffectSystem blockEffect;
    private PlayerControlState playerControl;
    private HudState hud;

    private EntityId gameStateId;
    private boolean resolved;

    @Override
    protected void initialize(Application app) {
        SimpleApplication simpleApp = (SimpleApplication) app;

        ed = new DefaultEntityData();
        modelView = new ModelViewState(ed, simpleApp.getRootNode());
        getStateManager().attach(modelView);

        environment = new EnvironmentState();
        getStateManager().attach(environment);

        roundSystem = new RoundSystem(ed);
        roundSystem.initialize();
        gameStateId = roundSystem.getGameStateId();

        EntityId mascotId = ed.createEntity();
        ed.setComponents(mascotId, new MascotComponent(), new PositionComponent(0f, 0f, 0f));

        EntityId playerId = ed.createEntity();
        ed.setComponents(playerId,
                new WeaponComponent(WeaponType.SWORD),
                new PositionComponent(0f, 0f, 8f),
                new PlayerHealthComponent(PLAYER_MAX_HEALTH));

        victorySystem = new VictorySystem(ed, gameStateId);
        npcBuilder = new NpcBuilderSystem(ed, roundSystem, mascotId);
        blockEffect = new BlockEffectSystem(ed, playerId, gameStateId);

        playerControl = new PlayerControlState(ed, playerId, gameStateId, blockEffect);
        getStateManager().attach(playerControl);

        hud = new HudState(ed, gameStateId, playerId);
        getStateManager().attach(hud);

        app.getCamera().setLocation(new Vector3f(0f, 1.5f, 8f));
        app.getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    @Override
    protected void cleanup(Application app) {
        getStateManager().detach(hud);
        getStateManager().detach(playerControl);
        getStateManager().detach(environment);
        getStateManager().detach(modelView);
        victorySystem.close();
        npcBuilder.close();
        blockEffect.close();
        ed.close();
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        if (resolved) {
            return;
        }

        npcBuilder.update(tpf);
        roundSystem.update(tpf);
        victorySystem.update(tpf);
        blockEffect.update(tpf);

        Result result = ed.getComponent(gameStateId, GameResultComponent.class).result();
        if (result != Result.IN_PROGRESS) {
            resolved = true;
            getStateManager().detach(this);
            getStateManager().attach(new GameEndState(result));
        }
    }
}
