package com.azeem.backweapons;

import java.util.HashMap;
import java.util.Map;

public class BackWeaponAnimationTracker {

    public static class State {
        public boolean holdingNow;
        public float visibility;
    }

    private static final Map<Integer, State> STATES = new HashMap<>();

    public static State get(int entityId) {
        return STATES.computeIfAbsent(entityId, id -> new State());
    }
}