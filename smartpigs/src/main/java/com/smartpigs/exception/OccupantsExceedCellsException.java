package com.smartpigs.exception;

public class OccupantsExceedCellsException extends RuntimeException {

    public OccupantsExceedCellsException(final int occupantCount, final int cellCount) {
        super("The number of occupants (" + occupantCount + ")" +
                " exceeds the number of cells (" + cellCount + ").");
    }
}
