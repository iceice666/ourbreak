package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.WeaponComponent.WeaponType;

/** Pure formatting of HUD values — no renderer dependency, so it is unit-testable. */
public final class HudText {

    private HudText() {
    }

    public static String weapon(WeaponType type) {
        return "Weapon: " + type;
    }

    public static String round(int current) {
        return "Round " + current;
    }

    /** Remaining attack time as {@code M:SS}, seconds rounded up and clamped at zero. */
    public static String countdown(double remainingSeconds) {
        long total = (long) Math.ceil(Math.max(0.0, remainingSeconds));
        long minutes = total / 60;
        long seconds = total % 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : Long.toString(seconds));
    }

    public static String buildings(int count) {
        return "Buildings: " + count;
    }
}
