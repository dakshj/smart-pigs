package com.smartpigs.model;

import java.util.List;

public class Pig extends Occupant {

    private final Address address;
    private final List<Pig> neighbors;

    public Pig(final Cell occupiedCell, final Address address, final List<Pig> neighbors) {
        super(occupiedCell);
        this.address = address;
        this.neighbors = neighbors;
    }

    private Address getAddress() {
        return address;
    }

    private List<Pig> getNeighbors() {
        return neighbors;
    }
}
