package com.smartpigs.model;

import java.util.List;

public class Grid {

    private final List<List<Occupant>> occupants;

    public Grid(final List<List<Occupant>> occupants) {
        this.occupants = occupants;
    }

    private List<List<Occupant>> getOccupants() {
        return occupants;
    }
}
