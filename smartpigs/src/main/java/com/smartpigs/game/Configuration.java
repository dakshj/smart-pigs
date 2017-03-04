package com.smartpigs.game;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;

import java.util.Set;

public class Configuration {

    private final int noOfPigs;
    private final int noOfStones;
    private final int rows;
    private final int columns;
    private final int hopCount;
    private final long hopDelay;
    private final long attackEta;
    private final Cell attackedCell;
    private final Set<Pig> pigSet;

    private transient Pig closestPig;

    public Configuration(final int noOfPigs, final int noOfStones, final int rows, final int columns,
            final int hopCount, final int hopDelay, final long attackEta, final Cell attackedCell, final Set<Pig> pigSet) {
        this.noOfPigs = noOfPigs;
        this.noOfStones = noOfStones;
        this.rows = rows;
        this.columns = columns;
        this.hopCount = hopCount;
        this.hopDelay = hopDelay;
        this.attackEta = attackEta;
        this.attackedCell = attackedCell;
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

    public int getHopCount() {
        return hopCount;
    }

    public long getHopDelay() {
        return hopDelay;
    }

    long getAttackEta() {
        return attackEta;
    }

    Cell getAttackedCell() {
        return attackedCell;
    }

    public Set<Pig> getPigSet() {
        return pigSet;
    }

    Pig getPigFromId(final String pigId) {
        //noinspection OptionalGetWithoutIsPresent
        return getPigSet().stream()
                .filter(pig -> pig.getId().equals(pigId))
                .findFirst()
                .get();
    }

    Pig getClosestPig() {
        return closestPig;
    }

    void setClosestPig(final Pig closestPig) {
        this.closestPig = closestPig;
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
                ", closestPig=" + closestPig +
                '}';
    }
}
