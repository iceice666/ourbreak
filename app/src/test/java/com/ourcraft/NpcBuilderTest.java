package com.ourcraft;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.MascotComponent;
import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.ourcraft.ecs.components.RoundComponent;
import com.ourcraft.ecs.systems.NpcBuilderSystem;
import com.ourcraft.ecs.systems.RoundSystem;
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
import java.util.stream.Stream;

import static com.ourcraft.ecs.components.BlockComponent.BlockType.CORAL;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.JELLYFISH;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.ROCK;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SAND;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SHELL;
import static com.ourcraft.ecs.components.GameResultComponent.Result.LOSS;
import static com.ourcraft.ecs.components.GameResultComponent.Result.WIN;
import static com.ourcraft.ecs.components.PhaseComponent.Phase.ATTACK;
import static com.ourcraft.ecs.components.PhaseComponent.Phase.BUILD;
import static com.ourcraft.ecs.systems.NpcBuilderSystem.blocksForRound;
import static com.ourcraft.ecs.systems.RoundSystem.ATTACK_DURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
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
    void firstRingUsesDeterministicFrontSideAndRearOrder() {
        placeFirstRing();

        assertIterableEquals(List.of(
                new PositionComponent(0.0f, 0.0f, 1.0f),
                new PositionComponent(-1.0f, 0.0f, 0.0f),
                new PositionComponent(1.0f, 0.0f, 0.0f),
                new PositionComponent(-1.0f, 0.0f, 1.0f),
                new PositionComponent(1.0f, 0.0f, 1.0f),
                new PositionComponent(-1.0f, 0.0f, -1.0f),
                new PositionComponent(1.0f, 0.0f, -1.0f),
                new PositionComponent(0.0f, 0.0f, -1.0f)
        ), placedBlocks().stream().map(PlacedBlock::position).toList());
    }

    @Test
    void positionsAreTranslatedFromMascotAndPreserveItsHeight() {
        ed.setComponent(mascotId, new PositionComponent(10.0f, 3.0f, -4.0f));

        placeFirstRing();

        assertIterableEquals(List.of(
                new PositionComponent(10.0f, 3.0f, -3.0f),
                new PositionComponent(9.0f, 3.0f, -4.0f),
                new PositionComponent(11.0f, 3.0f, -4.0f),
                new PositionComponent(9.0f, 3.0f, -3.0f),
                new PositionComponent(11.0f, 3.0f, -3.0f),
                new PositionComponent(9.0f, 3.0f, -5.0f),
                new PositionComponent(11.0f, 3.0f, -5.0f),
                new PositionComponent(10.0f, 3.0f, -5.0f)
        ), placedBlocks().stream().map(PlacedBlock::position).toList());
    }

    @Test
    void eachRoundUsesItsOrderedRepeatingBlockScript() {
        assertRoundTypes(SAND);
        advanceToNextBuild();
        assertRoundTypes(SAND, CORAL);
        advanceToNextBuild();
        assertRoundTypes(ROCK, SHELL);
        advanceToNextBuild();
        assertRoundTypes(ROCK, JELLYFISH);
    }

    @Test
    void occupiedPriorityPositionIsSkipped() {
        createBlock(SHELL, new PositionComponent(0.0f, 0.0f, 1.0f));

        builder.update(0.0f);

        assertEquals(
                new PositionComponent(-1.0f, 0.0f, 0.0f),
                placedBlocks().get(1).position());
    }

    @Test
    void fullyOccupiedFirstRingPlacesNextBlockAtSecondRingFrontCenter() {
        List<PositionComponent> firstRing = List.of(
                new PositionComponent(0.0f, 0.0f, 1.0f),
                new PositionComponent(-1.0f, 0.0f, 0.0f),
                new PositionComponent(1.0f, 0.0f, 0.0f),
                new PositionComponent(-1.0f, 0.0f, 1.0f),
                new PositionComponent(1.0f, 0.0f, 1.0f),
                new PositionComponent(-1.0f, 0.0f, -1.0f),
                new PositionComponent(1.0f, 0.0f, -1.0f),
                new PositionComponent(0.0f, 0.0f, -1.0f));
        firstRing.forEach(position -> createBlock(SHELL, position));

        builder.update(0.0f);

        assertEquals(SAND, blockAt(new PositionComponent(0.0f, 0.0f, 2.0f)).block().blockType());
    }

    @Test
    void destroyedPriorityPositionIsRefilledFirstInLaterRound() {
        completeBuild();
        PositionComponent front = new PositionComponent(0.0f, 0.0f, 1.0f);
        ed.removeEntity(blockAt(front).id());
        advanceToNextBuild();

        builder.update(0.0f);

        PlacedBlock newest = placedBlocks().getLast();
        assertEquals(front, newest.position());
        assertEquals(SAND, newest.block().blockType());
    }

    @Test
    void survivorsRemainUnchangedWhileLaterRoundsAddBlocks() {
        completeBuild();
        PlacedBlock front = blockAt(new PositionComponent(0.0f, 0.0f, 1.0f));
        ed.setComponent(front.id(), front.block().applyDamage(0.5f));
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

    @Test
    void unsupportedRoundIsRejectedBeforeBlockCreation() {
        ed.setComponent(gameStateId, new RoundComponent(5, 5, ATTACK_DURATION));

        assertThrows(IllegalStateException.class, () -> builder.update(0.0f));
        assertEquals(0, placedBlocks().size());
    }

    private void completeBuild() {
        int quota = blocksForRound(round().currentRound());
        for (int i = 0; i < quota; i++) {
            builder.update(0.0f);
        }
    }

    /** Places exactly the first ring (8 cells) for placement-order assertions. */
    private void placeFirstRing() {
        for (int i = 0; i < 8; i++) {
            builder.update(0.0f);
        }
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

    private void assertRoundTypes(BlockComponent.BlockType... script) {
        int firstNewBlock = placedBlocks().size();
        int quota = blocksForRound(round().currentRound());

        completeBuild();

        List<BlockComponent.BlockType> placedTypes = placedBlocks().stream()
                .skip(firstNewBlock)
                .map(placed -> placed.block().blockType())
                .toList();
        List<BlockComponent.BlockType> expectedTypes = java.util.stream.IntStream
                .range(0, quota)
                .mapToObj(index -> script[index % script.length])
                .toList();
        assertIterableEquals(expectedTypes, placedTypes);
    }

    private void advanceToNextBuild() {
        roundSystem.update((float) ATTACK_DURATION + 1.0f);
        assertEquals(BUILD, phase());
    }

    private EntityId createBlock(BlockComponent.BlockType blockType, PositionComponent position) {
        EntityId blockId = ed.createEntity();
        ed.setComponents(blockId,
                new BlockComponent(blockType),
                position,
                new ModelComponent(blockType.name().toLowerCase(Locale.ROOT) + "-block"));
        return blockId;
    }

    private PlacedBlock blockAt(PositionComponent position) {
        return placedBlocks().stream()
                .filter(block -> block.position().equals(position))
                .findFirst()
                .orElseThrow();
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
