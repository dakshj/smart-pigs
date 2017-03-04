package com.smartpigs.model;

import com.smartpigs.enums.OccupantType;

class Occupant {

    private final Cell occupiedCell;
    private final OccupantType occupantType;

    Occupant(final Cell occupiedCell, final OccupantType occupantType) {
        this.occupiedCell = occupiedCell;
        this.occupantType = occupantType;
    }

    private Cell getOccupiedCell() {
        return occupiedCell;
    }

    private OccupantType getOccupantType() {
        return occupantType;
    }
}
