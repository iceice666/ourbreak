## REMOVED Requirements

### Requirement: Player reflect health
**Reason**: Player health is removed entirely — the game models no player hit points. Shell's consequence is now the split mechanic (see the `shell-splitting` capability), not reflect damage.
**Migration**: Delete `PlayerHealthComponent` and all reads of it; Shell behaviour moves to `shell-splitting`.

### Requirement: Shell on-destroy reflect
**Reason**: Shell no longer reflects damage; with no player health there is nothing to reduce. A Shell hit by the wrong weapon splits instead.
**Migration**: See the `shell-splitting` capability for the replacement behaviour.
