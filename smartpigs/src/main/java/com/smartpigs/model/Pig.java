package com.smartpigs.model;

import java.io.Serializable;

public class Pig extends Occupant implements Serializable {

    private String id;
    private Address address;
    private boolean alive = true;

    public String getId() {
        return id;
    }

    public Address getAddress() {
        return address;
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
                "} " + super.toString();
    }

    private boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }
}
