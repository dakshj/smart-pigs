package com.smartpigs.game;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Configuration {

    private final int noOfPigs;
    private final int noOfStones;
    private final int rows;
    private final int columns;
    private final int maxHopCount;
    private final long hopDelay;
    private final long attackEta;
    private final Cell attackedCell;
    private final Set<Pig> pigSet;

    private transient Pig closestPig;
    private transient Map<Pig, Set<Pig>> peerMap;
    private transient Map<Pig, List<List<Occupant>>> neighborMap;

    public Configuration(final int noOfPigs, final int noOfStones, final int rows, final int columns,
            final int maxHopCount, final int hopDelay, final long attackEta, final Cell attackedCell, final Set<Pig> pigSet) {
        this.noOfPigs = noOfPigs;
        this.noOfStones = noOfStones;
        this.rows = rows;
        this.columns = columns;
        this.maxHopCount = maxHopCount;
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

    public int getMaxHopCount() {
        return maxHopCount;
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

    public Set<Pig> getFromPeerMap(final Pig pig) {
        if (peerMap == null) {
            return null;
        }

        return peerMap.get(pig);
    }

    void putInPeerMap(final Pig pig, final Set<Pig> peers) {
        if (peerMap == null) {
            peerMap = new HashMap<>();
        }

        peerMap.put(pig, peers);
    }

    public List<List<Occupant>> getFromNeighborMap(final Pig pig) {
        if (neighborMap == null) {
            return null;
        }

        return neighborMap.get(pig);
    }

    void putInNeighborMap(final Pig pig, final List<List<Occupant>> neighbors) {
        if (neighborMap == null) {
            neighborMap = new HashMap<>();
        }

        neighborMap.put(pig, neighbors);
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "noOfPigs=" + noOfPigs +
                ", noOfStones=" + noOfStones +
                ", rows=" + rows +
                ", columns=" + columns +
                ", maxHopCount=" + maxHopCount +
                ", hopDelay=" + hopDelay +
                ", attackEta=" + attackEta +
                ", attackedCell=" + attackedCell +
                ", pigSet=" + pigSet +
                ", closestPig=" + closestPig +
                ", peerMap=" + peerMap +
                ", neighborMap=" + neighborMap +
                '}';
    }
}
