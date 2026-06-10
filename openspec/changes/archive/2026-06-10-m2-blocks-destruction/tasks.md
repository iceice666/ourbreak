## 1. ECS Components

- [x] 1.1 Replace the empty `BlockComponent` stub with `BlockType`, current durability, maximum durability, standard type values, full-health construction, and clamped damage behavior.
- [x] 1.2 Add the `WeaponComponent` and `WeaponType` definitions for SWORD, GUN, and DRONE.
- [x] 1.3 Add the empty `MascotComponent` marker record.
- [x] 1.4 Update existing M1 tests and block construction sites to use typed full-health blocks.

## 2. Block Durability Tests

- [x] 2.1 Add `BlockTest` coverage for the standard durability of all five block types and full-health construction.
- [x] 2.2 Add `BlockTest` coverage for nonlethal damage and overkill clamping to zero.

## 3. Weapon Damage System

- [x] 3.1 Add `WeaponSystem` with named base, strong, weak, and neutral placeholder constants plus the complete weapon counter matrix.
- [x] 3.2 Implement ATTACK-phase gating and validation that the attacking player has a `WeaponComponent`.
- [x] 3.3 Implement distinct-target processing that ignores missing and non-block targets.
- [x] 3.4 Apply calculated damage by replacing nonlethal `BlockComponent` state and removing entities on lethal damage.

## 4. Weapon System Tests

- [x] 4.1 Add `WeaponTest` coverage for all weapon types and neutral, strong, and weak multiplier behavior.
- [x] 4.2 Add `WeaponTest` coverage for BUILD-phase rejection and missing player weapons.
- [x] 4.3 Add `WeaponTest` coverage for multiple, duplicate, missing, and non-block targets.
- [x] 4.4 Add `WeaponTest` coverage for nonlethal durability updates and lethal entity removal.

## 5. Verification

- [x] 5.1 Run `nix develop -c ./gradlew test` and resolve all failures.
- [x] 5.2 Confirm the implementation remains headless and adds no Gradle dependencies or M4 targeting logic.
