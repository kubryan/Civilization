package com.civilizationmod;

/** Non-persistent result returned by a building marker scan command. */
public record BuildingScanSummary(
        int detected,
        int updated,
        int bound,
        int valid,
        int invalid,
        int townHallsRegistered,
        int townHallConflicts
) {
    public BuildingScanSummary(int detected, int updated, int bound) {
        this(detected, updated, bound, 0, 0, 0, 0);
    }

    public BuildingScanSummary(int detected, int updated, int bound, int valid, int invalid) {
        this(detected, updated, bound, valid, invalid, 0, 0);
    }

    public BuildingScanSummary {
        detected = Math.max(0, detected);
        updated = Math.max(0, updated);
        bound = Math.max(0, bound);
        valid = Math.max(0, valid);
        invalid = Math.max(0, invalid);
        townHallsRegistered = Math.max(0, townHallsRegistered);
        townHallConflicts = Math.max(0, townHallConflicts);
    }
}
