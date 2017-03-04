package com.smartpigs.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Pig extends Occupant implements Serializable {

    private String id;
    private Address address;
    private Set<Address> neighborAddresses;

    public String getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    private Set<Address> getNeighborAddresses() {
        return neighborAddresses;
    }

    public void addNeighborAddress(final Address neighborAddress) {
        if (neighborAddresses == null) {
            neighborAddresses = new HashSet<>();
        }

        neighborAddresses.add(neighborAddress);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Pig pig = (Pig) o;

        return id.equals(pig.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Pig{" +
                "id='" + id + '\'' +
                ", address=" + address +
                ", neighborAddresses=" + neighborAddresses +
                "} " + super.toString();
    }
}
