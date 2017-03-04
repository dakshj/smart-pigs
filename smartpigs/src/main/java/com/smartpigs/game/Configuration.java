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

    // TODO read adjacency list as well

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

    int getNoOfPigs() {
        return noOfPigs;
    }

    int getNoOfStones() {
        return noOfStones;
    }

    int getRows() {
        return rows;
    }

    int getColumns() {
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

    Pig getPigFromId(final String pigId) {
        //noinspection OptionalGetWithoutIsPresent
        return getPigSet().stream()
                .filter(pig -> pig.getId().equals(pigId))
                .findFirst()
                .get();
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "noOfPigs=" + noOfPigs +
                ", noOfStones=" + noOfStones +
                ", rows=" + rows +
                ", columns=" + columns +
                ", hopCount=" + hopCount +
                ", hopDelay=" + hopDelay +
                ", pigSet=" + pigSet +
                '}';
    }
}
