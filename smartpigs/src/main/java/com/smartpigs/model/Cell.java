package com.smartpigs.model;

import java.io.Serializable;

public class Cell implements Serializable {

    private final int row;
    private final int col;

    public Cell(final int row, final int col) {
        this.row = row;
        this.col = col;
    }

    private int getRow() {
        return row;
    }

    private int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return row + "," + col;
    }
}
