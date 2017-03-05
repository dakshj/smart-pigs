package com.smartpigs.model;

import java.util.List;

public class Grid {

    private final List<List<Occupant>> occupants;

    public Grid(final List<List<Occupant>> occupants) {
        this.occupants = occupants;
    }

    public List<List<Occupant>> getOccupants() {
        return occupants;
    }

    @Override
    public String toString() {
        // TODO print grid in a prettier manner

        return "Grid{" +
                "occupants=" + occupants +
                '}';
    }
}
