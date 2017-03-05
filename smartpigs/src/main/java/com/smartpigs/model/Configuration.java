package com.smartpigs.model;

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
    private final Address gameServerAddress;
    private final Set<Pig> pigSet;

    private transient Pig closestPig;
    private transient Map<Pig, Set<Pig>> peerMap;
    private transient Map<Pig, List<List<Occupant>>> neighborMap;
    private Grid grid;

    public Configuration(final int noOfPigs, final int noOfStones, final int rows, final int columns,
            final int maxHopCount, final int hopDelay, final long attackEta, final Cell attackedCell,
            final Address gameServerAddress, final Set<Pig> pigSet) {
        this.noOfPigs = noOfPigs;
        this.noOfStones = noOfStones;
        this.rows = rows;
        this.columns = columns;
        this.maxHopCount = maxHopCount;
        this.hopDelay = hopDelay;
        this.attackEta = attackEta;
        this.attackedCell = attackedCell;
        this.gameServerAddress = gameServerAddress;
        this.pigSet = pigSet;
    }

    public int getNoOfPigs() {
        return noOfPigs;
    }

    public int getNoOfStones() {
        return noOfStones;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getMaxHopCount() {
        return maxHopCount;
    }

    public long getHopDelay() {
        return hopDelay;
    }

    public long getAttackEta() {
        return attackEta;
    }

    public Cell getAttackedCell() {
        return attackedCell;
    }

    public Address getGameServerAddress() {
        return gameServerAddress;
    }

    public Set<Pig> getPigSet() {
        return pigSet;
    }

    public Pig getPigFromId(final String pigId) {
        //noinspection OptionalGetWithoutIsPresent
        return getPigSet().stream()
                .filter(pig -> pig.getId().equals(pigId))
                .findFirst()
                .get();
    }

    public Pig getClosestPig() {
        return closestPig;
    }

    public void setClosestPig(final Pig closestPig) {
        this.closestPig = closestPig;
    }

    public Set<Pig> getFromPeerMap(final Pig pig) {
        if (peerMap == null) {
            return null;
        }

        return peerMap.get(pig);
    }

    public void putInPeerMap(final Pig pig, final Set<Pig> peers) {
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

    public void putInNeighborMap(final Pig pig, final List<List<Occupant>> neighbors) {
        if (neighborMap == null) {
            neighborMap = new HashMap<>();
        }

        neighborMap.put(pig, neighbors);
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(final Grid grid) {
        this.grid = grid;
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
                ", gameServerAddress=" + gameServerAddress +
                ", pigSet=" + pigSet +
                ", closestPig=" + closestPig +
                ", peerMap=" + peerMap +
                ", neighborMap=" + neighborMap +
                ", grid=" + grid +
                '}';
    }
}
