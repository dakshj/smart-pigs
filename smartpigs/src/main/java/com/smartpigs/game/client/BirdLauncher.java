package com.smartpigs.game.client;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BirdLauncher {

    private final Pig closestPig;
    private final long attackEta;
    private final Cell attackedCell;
    private final int maxHopCount;

    public BirdLauncher(final Pig closestPig, final long attackEta, final Cell attackedCell,
            final int maxHopCount) {
        this.closestPig = closestPig;
        this.attackEta = attackEta;
        this.attackedCell = attackedCell;
        this.maxHopCount = maxHopCount;
    }

    /**
     * Launches a bird to the closest pig.
     * <p>
     * Utilizes {@link PigServer#birdApproaching(List, long, Cell, int)} to simulate a bird launch.
     */
    public void launch() {
        try {
            PigServer.connect(closestPig)
                    .birdApproaching(new ArrayList<>(), attackEta, attackedCell, maxHopCount);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
