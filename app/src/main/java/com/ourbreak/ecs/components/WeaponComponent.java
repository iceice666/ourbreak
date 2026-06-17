package com.ourbreak.ecs.components;

import com.simsilica.es.EntityComponent;

import java.util.Objects;

public record WeaponComponent(WeaponType weaponType) implements EntityComponent {

    public WeaponComponent {
        Objects.requireNonNull(weaponType, "weaponType");
    }

    public enum WeaponType {
        SWORD,
        GUN,
        DRONE
    }
}
