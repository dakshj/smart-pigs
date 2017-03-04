package com.smartpigs.enums;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;

import java.io.Serializable;

public enum OccupantType implements Serializable {

    /**
     * Indicates that the {@link Cell} is empty.
     */
    EMPTY,

    /**
     * Indicates that the {@link Cell} is occupied by a {@link Pig}.
     */
    PIG,

    /**
     * Indicates that the {@link Cell} is occupied by a stone.
     */
    STONE
}
