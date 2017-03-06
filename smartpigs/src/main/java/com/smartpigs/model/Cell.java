package com.smartpigs.model;

import java.io.Serializable;

public class Cell implements Serializable {

    private final int row;
    private final int col;

    public Cell(final int row, final int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Cell cell = (Cell) o;

        return row == cell.row && col == cell.col;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        return result;
    }

    @Override
    public String toString() {
        return "{" + row + ", " + col + "}";
    }
}
