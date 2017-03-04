package com.smartpigs.game;

import com.smartpigs.model.Pig;

import java.util.Set;

public class Configuration {

    private final int noOfPigs;
    private final int noOfStones;
    private final int rows;
    private final int columns;
    private final int hopCount;
    private final int hopDelay;
    private final Set<Pig> pigSet;

    public Configuration(final int noOfPigs, final int noOfStones, final int rows, final int columns,
            final int hopCount, final int hopDelay, final Set<Pig> pigSet) {
        this.noOfPigs = noOfPigs;
        this.noOfStones = noOfStones;
        this.rows = rows;
        this.columns = columns;
        this.hopCount = hopCount;
        this.hopDelay = hopDelay;
        this.pigSet = pigSet;
    }

    private int getNoOfPigs() {
        return noOfPigs;
    }

    private int getNoOfStones() {
        return noOfStones;
    }

    private int getRows() {
        return rows;
    }

    private int getColumns() {
        return columns;
    }

    private int getHopCount() {
        return hopCount;
    }

    private int getHopDelay() {
        return hopDelay;
    }

    private Set<Pig> getPigSet() {
        return pigSet;
    }
}
