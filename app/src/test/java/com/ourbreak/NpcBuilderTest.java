package com.ourbreak;

import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.BlockComponent.BlockType;
import com.ourbreak.ecs.components.GameResultComponent;
import com.ourbreak.ecs.components.MascotComponent;
import com.ourbreak.ecs.components.ModelComponent;
import com.ourbreak.ecs.components.PhaseComponent;
import com.ourbreak.ecs.components.PositionComponent;
import com.ourbreak.ecs.components.RoundComponent;
import com.ourbreak.ecs.systems.NpcBuilderSystem;
import com.ourbreak.ecs.systems.RoundSystem;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static com.ourbreak.ecs.components.BlockComponent.BlockType.CORAL;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.JELLYFISH;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.ROCK;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.SAND;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.SHELL;
import static com.ourbreak.ecs.components.GameResultComponent.Result.LOSS;
import static com.ourbreak.ecs.components.GameResultComponent.Result.WIN;
import static com.ourbreak.ecs.components.PhaseComponent.Phase.ATTACK;
import static com.ourbreak.ecs.components.PhaseComponent.Phase.BUILD;
import static com.ourbreak.ecs.systems.NpcBuilderSystem.blocksForRound;
import static com.ourbreak.ecs.systems.RoundSystem.ATTACK_DURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcBuilderTest {

    private EntityData ed;
    private RoundSystem roundSystem;
    private NpcBuilderSystem builder;
    private EntityId gameStateId;
    private EntityId mascotId;

    @BeforeEach
    void setUp() {
        ed = new DefaultEntityData();
        roundSystem = new RoundSystem(ed);
        roundSystem.initialize();
        gameStateId = roundSystem.getGameStateId();

        mascotId = ed.createEntity();
        ed.setComponents(mascotId,
                new MascotComponent(),
                new PositionComponent(0.0f, 0.0f, 0.0f));
        builder = new NpcBuilderSystem(ed, roundSystem, mascotId);
    }

    @AfterEach
    void tearDown() {
        builder.close();
    }

    @Test
    void activeBuildPlacesOneBlockPerUpdateAndTransitionsAfterQuota() {
        int quota = blocksForRound(1);
        builder.update(0.0f);
        assertEquals(1, placedBlocks().size());
        assertEquals(BUILD, phase());

        for (int i = 1; i < quota - 1; i++) {
            builder.update(0.0f);
        }

        assertEquals(quota - 1, placedBlocks().size());
        assertEquals(BUILD, phase());

        builder.update(0.0f);

        assertEquals(quota, placedBlocks().size());
        assertEquals(ATTACK, phase());
        assertEquals(ATTACK_DURATION, round().remainingSeconds(), 0.001);
    }

    @Test
    void attackPhaseUpdateCreatesNoBlocks() {
        roundSystem.beginAttackPhase();

        builder.update(0.0f);

        assertEquals(0, placedBlocks().size());
    }

    @Test
    void eachRoundPlacesOnlyTypesFromItsPalette() {
        // The village draws block types from the round's palette, so the type progression (and the
        // counter-matrix) is preserved even though exact placement is now procedural.
        assertTypesWithin(Set.of(SAND));
        advanceToNextBuild();
        assertTypesWithin(Set.of(SAND, CORAL));
        advanceToNextBuild();
        assertTypesWithin(Set.of(ROCK, SHELL));
        advanceToNextBuild();
        assertTypesWithin(Set.of(ROCK, JELLYFISH));
        advanceToNextBuild();
        assertTypesWithin(Set.of(ROCK, SHELL, JELLYFISH, CORAL)); // round 5+: the full gauntlet
    }

    @Test
    void villageBuildsAroundTheMascotAboveItsBase() {
        ed.setComponent(mascotId, new PositionComponent(10.0f, 3.0f, -4.0f));
        completeBuild();

        for (PlacedBlock placed : placedBlocks()) {
            PositionComponent p = placed.position();
            // Houses sit on or above the mascot's base height, and clear of the central crab plaza.
            assertTrue(p.y() >= 3.0f, () -> "block below mascot base: " + p);
            float dx = p.x() - 10.0f;
            float dz = p.z() - (-4.0f);
            assertTrue(Math.abs(dx) > 0.5f || Math.abs(dz) > 0.5f, () -> "block on the mascot cell: " + p);
        }
    }

    @Test
    void survivorsRemainUnchangedWhileLaterRoundsAddBlocks() {
        completeBuild();
        PlacedBlock damaged = placedBlocks().get(0);
        ed.setComponent(damaged.id(), damaged.block().applyDamage(0.5f));
        List<PlacedBlock> survivors = placedBlocks();

        for (int round = 2; round <= 4; round++) {
            advanceToNextBuild();
            int blockCountBeforeBuild = placedBlocks().size();

            completeBuild();

            List<PlacedBlock> currentBlocks = placedBlocks();
            assertEquals(blockCountBeforeBuild + blocksForRound(round), currentBlocks.size());
            assertTrue(currentBlocks.containsAll(survivors));
        }
    }

    @Test
    void placedBlocksHaveFullDurabilityAndTypeDerivedModels() {
        completeAllRounds();

        for (PlacedBlock placed : placedBlocks()) {
            BlockComponent block = placed.block();
            assertEquals(block.blockType().standardDurability(), block.durability());
            assertEquals(block.blockType().standardDurability(), block.maxDurability());
            assertEquals(
                    block.blockType().name().toLowerCase(Locale.ROOT) + "-block",
                    placed.model().modelId());
        }
    }

    @Test
    void attackPhaseUpdatesAreNoOpsAfterConstruction() {
        completeBuild();

        builder.update(0.0f);
        builder.update(0.0f);

        assertEquals(blocksForRound(1), placedBlocks().size());
    }

    @Test
    void completedGameUpdatesAreNoOps() {
        completeBuild();
        advanceToNextBuild();

        ed.setComponent(gameStateId, new GameResultComponent(WIN));
        builder.update(0.0f);

        ed.setComponent(gameStateId, new GameResultComponent(LOSS));
        builder.update(0.0f);

        assertEquals(blocksForRound(1), placedBlocks().size());
        assertEquals(BUILD, phase());
    }

    @Test
    void missingMascotPositionIsRejectedBeforeBlockCreation() {
        ed.removeComponent(mascotId, PositionComponent.class);

        assertThrows(IllegalStateException.class, () -> builder.update(0.0f));
        assertEquals(0, placedBlocks().size());
    }

    @ParameterizedTest
    @MethodSource("requiredGameStateComponentTypes")
    void missingRequiredGameStateIsRejectedBeforeBlockCreation(
            Class<? extends EntityComponent> componentType
    ) {
        ed.removeComponent(gameStateId, componentType);

        assertThrows(IllegalStateException.class, () -> builder.update(0.0f));
        assertEquals(0, placedBlocks().size());
    }

    private void completeBuild() {
        int quota = blocksForRound(round().currentRound());
        for (int i = 0; i < quota; i++) {
            builder.update(0.0f);
        }
    }

    private void assertTypesWithin(Set<BlockType> palette) {
        int firstNewBlock = placedBlocks().size();
        completeBuild();
        placedBlocks().stream()
                .skip(firstNewBlock)
                .forEach(placed -> assertTrue(palette.contains(placed.block().blockType()),
                        () -> placed.block().blockType() + " is not in the round palette " + palette));
    }

    private static Stream<Class<? extends EntityComponent>> requiredGameStateComponentTypes() {
        return Stream.of(
                RoundComponent.class,
                PhaseComponent.class,
                GameResultComponent.class);
    }

    private void completeAllRounds() {
        for (int round = 1; round <= 4; round++) {
            completeBuild();
            if (round < 4) {
                advanceToNextBuild();
            }
        }
    }

    private void advanceToNextBuild() {
        roundSystem.advanceRound();
        assertEquals(BUILD, phase());
    }

    private List<PlacedBlock> placedBlocks() {
        EntitySet blocks = ed.getEntities(
                BlockComponent.class,
                PositionComponent.class,
                ModelComponent.class);
        try {
            return blocks.stream()
                    .sorted(Comparator.comparing(Entity::getId))
                    .map(this::placedBlock)
                    .toList();
        } finally {
            blocks.release();
        }
    }

    private PlacedBlock placedBlock(Entity entity) {
        return new PlacedBlock(
                entity.getId(),
                entity.get(BlockComponent.class),
                entity.get(PositionComponent.class),
                entity.get(ModelComponent.class));
    }

    private PhaseComponent.Phase phase() {
        return ed.getComponent(gameStateId, PhaseComponent.class).phase();
    }

    private RoundComponent round() {
        return ed.getComponent(gameStateId, RoundComponent.class);
    }

    private record PlacedBlock(
            EntityId id,
            BlockComponent block,
            PositionComponent position,
            ModelComponent model
    ) {
    }
}
