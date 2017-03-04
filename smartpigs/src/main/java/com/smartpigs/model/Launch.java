package com.smartpigs.model;

public class Launch {

    private final Cell destinationCell;
    private final long etaMillis;

    private Launch(final Cell destinationCell, final long etaMillis) {
        this.destinationCell = destinationCell;
        this.etaMillis = etaMillis;
    }

    private Cell getDestinationCell() {
        return destinationCell;
    }

    private long getEtaMillis() {
        return etaMillis;
    }
}
