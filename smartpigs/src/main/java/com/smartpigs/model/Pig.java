package com.smartpigs.model;

import java.io.Serializable;

public class Pig extends Occupant implements Serializable {

    private String id;
    private Address address;
    private boolean hit;
    private boolean birdApproachingMessageReceived;

    public String getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public boolean wasHit() {
        return hit;
    }

    public void setHit() {
        hit = true;
    }

    public boolean hasReceivedBirdApproachingMessage() {
        return birdApproachingMessageReceived;
    }

    public void setHasReceivedBirdApproachingMessage() {
        this.birdApproachingMessageReceived = true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Pig pig = (Pig) o;

        return hit == pig.hit && id.equals(pig.id) && address.equals(pig.address);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + (hit ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "P" + id;
    }
}
