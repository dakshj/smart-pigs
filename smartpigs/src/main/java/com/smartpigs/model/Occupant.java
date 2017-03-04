package com.smartpigs.model;

import com.smartpigs.enums.OccupantType;

import java.io.Serializable;

public class Occupant implements Serializable {

    private Cell occupiedCell;
    private OccupantType occupantType;

    private Cell getOccupiedCell() {
        return occupiedCell;
    }

    public void setOccupiedCell(final Cell occupiedCell) {
        this.occupiedCell = occupiedCell;
    }

    private OccupantType getOccupantType() {
        return occupantType;
    }

    public void setOccupantType(final OccupantType occupantType) {
        this.occupantType = occupantType;
    }

    @Override
    public String toString() {
        return "Occupant{" +
                "occupiedCell=" + occupiedCell +
                ", occupantType=" + occupantType +
                '}';
    }
}
