package com.ourcraft;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.BlockComponent.BlockType;
import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.MascotComponent;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.WeaponComponent;
import com.ourcraft.ecs.components.WeaponComponent.WeaponType;
import com.ourcraft.ecs.systems.WeaponSystem;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.ourcraft.ecs.components.BlockComponent.BlockType.CORAL;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.JELLYFISH;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.ROCK;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SAND;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SHELL;
import static com.ourcraft.ecs.components.WeaponComponent.WeaponType.DRONE;
import static com.ourcraft.ecs.components.WeaponComponent.WeaponType.GUN;
import static com.ourcraft.ecs.components.WeaponComponent.WeaponType.SWORD;
import static com.ourcraft.ecs.systems.WeaponSystem.NEUTRAL_MULTIPLIER;
import static com.ourcraft.ecs.systems.WeaponSystem.STRONG_MULTIPLIER;
import static com.ourcraft.ecs.systems.WeaponSystem.WEAK_MULTIPLIER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeaponTest {

    private EntityData ed;
    private EntityId gameStateId;
    private WeaponSystem system;

    @BeforeEach
    void setUp() {
        ed = new DefaultEntityData();
        gameStateId = ed.createEntity();
        ed.setComponents(gameStateId,
                new PhaseComponent(Phase.ATTACK),
                new GameResultComponent(Result.IN_PROGRESS));
        system = new WeaponSystem(ed, gameStateId);
    }

    @Test
    void completeCounterMatrixUsesAssignedMultipliers() {
        assertMultiplier(SWORD, SAND, STRONG_MULTIPLIER);
        assertMultiplier(SWORD, CORAL, WEAK_MULTIPLIER);
        assertMultiplier(SWORD, SHELL, WEAK_MULTIPLIER);
        assertMultiplier(SWORD, ROCK, NEUTRAL_MULTIPLIER);
        assertMultiplier(SWORD, JELLYFISH, NEUTRAL_MULTIPLIER);

        assertMultiplier(GUN, SAND, NEUTRAL_MULTIPLIER);
        assertMultiplier(GUN, CORAL, STRONG_MULTIPLIER);
        assertMultiplier(GUN, SHELL, NEUTRAL_MULTIPLIER);
        assertMultiplier(GUN, ROCK, WEAK_MULTIPLIER);
        assertMultiplier(GUN, JELLYFISH, STRONG_MULTIPLIER);

        assertMultiplier(DRONE, SAND, STRONG_MULTIPLIER);
        assertMultiplier(DRONE, CORAL, NEUTRAL_MULTIPLIER);
        assertMultiplier(DRONE, SHELL, WEAK_MULTIPLIER);
        assertMultiplier(DRONE, ROCK, STRONG_MULTIPLIER);
        assertMultiplier(DRONE, JELLYFISH, WEAK_MULTIPLIER);
    }

    @Test
    void buildPhaseDoesNotApplyDamage() {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(ROCK);
        ed.setComponent(gameStateId, new PhaseComponent(Phase.BUILD));

        system.attack(player, List.of(block));

        assertEquals(4.0f, block(block).durability());
    }

    @ParameterizedTest
    @EnumSource(value = Result.class, names = {"WIN", "LOSS"})
    void completedGameDoesNotApplyDamage(Result completedResult) {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(ROCK);
        ed.setComponent(gameStateId, new GameResultComponent(completedResult));

        system.attack(player, List.of(block));

        assertEquals(4.0f, block(block).durability());
    }

    @Test
    void playerWithoutWeaponIsRejected() {
        EntityId player = ed.createEntity();
        EntityId block = createBlock(ROCK);

        assertThrows(IllegalStateException.class, () -> system.attack(player, List.of(block)));
        assertEquals(4.0f, block(block).durability());
    }

    @Test
    void multipleDistinctTargetsEachReceiveDamage() {
        EntityId player = createPlayer(SWORD);
        EntityId first = createBlock(ROCK);
        EntityId second = createBlock(ROCK);

        system.attack(player, List.of(first, second));

        assertEquals(3.0f, block(first).durability());
        assertEquals(3.0f, block(second).durability());
    }

    @Test
    void duplicateTargetReceivesDamageOnce() {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(ROCK);

        system.attack(player, List.of(block, block, block));

        assertEquals(3.0f, block(block).durability());
    }

    @Test
    void emptyTargetCollectionIsNoOp() {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(ROCK);

        system.attack(player, List.of());

        assertEquals(4.0f, block(block).durability());
    }

    @Test
    void nullTargetIdsAreIgnored() {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(ROCK);

        system.attack(player, Arrays.asList(null, block, null));

        assertEquals(3.0f, block(block).durability());
    }

    @Test
    void nullTargetCollectionIsRejectedBeforeDamage() {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(ROCK);

        assertThrows(NullPointerException.class, () -> system.attack(player, null));
        assertEquals(4.0f, block(block).durability());
    }

    @ParameterizedTest
    @MethodSource("requiredGameStateComponentTypes")
    void missingGameStateEligibilityComponentIsRejectedBeforeDamage(
            Class<? extends EntityComponent> componentType
    ) {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(ROCK);
        ed.removeComponent(gameStateId, componentType);

        assertThrows(IllegalStateException.class, () -> system.attack(player, List.of(block)));
        assertEquals(4.0f, block(block).durability());
    }

    @Test
    void missingAndNonBlockTargetsAreIgnored() {
        EntityId player = createPlayer(SWORD);
        EntityId validBlock = createBlock(ROCK);
        EntityId missing = ed.createEntity();
        ed.removeEntity(missing);
        EntityId nonBlock = ed.createEntity();
        ed.setComponent(nonBlock, new MascotComponent());

        system.attack(player, List.of(missing, nonBlock, validBlock));

        assertEquals(3.0f, block(validBlock).durability());
        assertEquals(new MascotComponent(), ed.getComponent(nonBlock, MascotComponent.class));
    }

    @Test
    void nonlethalDamageReplacesBlockComponent() {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(CORAL);

        system.attack(player, List.of(block));

        assertEquals(1.5f, block(block).durability());
        assertEquals(2.0f, block(block).maxDurability());
    }

    @Test
    void lethalDamageRemovesBlockEntity() {
        EntityId player = createPlayer(SWORD);
        EntityId block = createBlock(SAND);

        system.attack(player, List.of(block));

        assertNull(ed.getComponent(block, BlockComponent.class));
    }

    private void assertMultiplier(WeaponType weaponType, BlockType blockType, float multiplier) {
        EntityId player = createPlayer(weaponType);
        EntityId target = ed.createEntity();
        ed.setComponent(target, new BlockComponent(blockType, 10.0f, 10.0f));

        system.attack(player, List.of(target));

        assertEquals(10.0f - multiplier, block(target).durability());
    }

    private static Stream<Class<? extends EntityComponent>> requiredGameStateComponentTypes() {
        return Stream.of(PhaseComponent.class, GameResultComponent.class);
    }

    private EntityId createPlayer(WeaponType weaponType) {
        EntityId player = ed.createEntity();
        ed.setComponent(player, new WeaponComponent(weaponType));
        return player;
    }

    private EntityId createBlock(BlockType blockType) {
        EntityId block = ed.createEntity();
        ed.setComponent(block, new BlockComponent(blockType));
        return block;
    }

    private BlockComponent block(EntityId blockId) {
        return ed.getComponent(blockId, BlockComponent.class);
    }
}
