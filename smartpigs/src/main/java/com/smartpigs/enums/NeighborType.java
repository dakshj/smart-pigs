package com.smartpigs.enums;

import com.smartpigs.model.Pig;

public enum NeighborType {

    /**
     * Indicates that the neighbor of a {@link Pig} is logical, i.e., connected via the
     * P2P network.
     */
    LOGICAL,

    /**
     * Indicates that the neighbor of a {@link Pig} is physically adjacent to its cell.
     */
    PHYSICAL
}
