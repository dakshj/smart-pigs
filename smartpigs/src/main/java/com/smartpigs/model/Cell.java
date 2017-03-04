package com.smartpigs.model;

public class Cell {

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
}
