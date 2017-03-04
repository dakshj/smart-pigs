package com.smartpigs.exception;

/**
 * Thrown when the sum of all occupants (pigs and stones) exceeds the maximum capacity of the grid.
 */
public class OccupantsExceedCellsException extends RuntimeException {

    /**
     * @param occupantCount The amount of occupants (pigs and stones)
     * @param cellCount     The amount of cells in the grid (rows x columns)
     */
    public OccupantsExceedCellsException(final int occupantCount, final int cellCount) {
        super("The number of occupants (" + occupantCount + ")" +
                " exceeds the number of cells (" + cellCount + ").");
    }
}
