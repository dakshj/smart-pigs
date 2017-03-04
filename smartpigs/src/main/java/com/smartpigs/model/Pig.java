package com.smartpigs.model;

import com.smartpigs.enums.OccupantType;

import java.util.HashSet;
import java.util.Set;

public class Pig extends Occupant {

    private final String id;
    private final Address address;
    private final Set<Pig> logicalNeighbors;

    public Pig(final Cell occupiedCell, final OccupantType occupantType,
            final String id, final Address address) {
        super(occupiedCell, occupantType);
        this.id = id;
        this.address = address;
        this.logicalNeighbors = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public Set<Pig> getLogicalNeighbors() {
        return logicalNeighbors;
    }

    public void addLogicalNeighbor(final Pig neighbor) {
        logicalNeighbors.add(neighbor);
    }

    @Override
    public String toString() {
        return "Pig{" +
                "id='" + id + '\'' +
                ", address=" + address +
                ", logicalNeighbors=" + logicalNeighbors +
                "} " + super.toString();
    }
}
