package com.civilizationmod;

import java.util.Arrays;

/** Stable logical functions that can be activated by building marker items. */
public enum BuildingFunction {
    WAREHOUSE("warehouse"),
    RESIDENCE("residence"),
    TOWN_HALL("town_hall");

    private final String id;

    BuildingFunction(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public static BuildingFunction fromId(String id) {
        return Arrays.stream(values())
                .filter(function -> function.id.equals(id))
                .findFirst()
                .orElse(null);
    }
}

