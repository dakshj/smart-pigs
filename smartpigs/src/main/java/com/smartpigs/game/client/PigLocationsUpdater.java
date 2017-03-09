package com.smartpigs.game.client;

import com.smartpigs.model.Grid;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Set;

public class PigLocationsUpdater {

    private final Set<Pig> pigSet;
    private final Grid grid;

    /**
     * @param pigSet The {@link Set} of pigs whose locations need to be updated
     * @param grid   The grid within which to update the updated locations
     */
    public PigLocationsUpdater(final Set<Pig> pigSet, final Grid grid) {
        this.pigSet = pigSet;
        this.grid = grid;
    }

    /**
     * Updates the cell of each pig in the provided grid, by querying all pigs using Java RMI.
     */
    public void update() {
        pigSet.forEach(oldPig -> {
            try {
                final Pig newPig = PigServer.connect(oldPig).getPig();

                grid.setOccupantAsEmpty(oldPig.getOccupiedCell());

                if (!newPig.getOccupiedCell().equals(oldPig.getOccupiedCell())) {
                    grid.setOccupant(newPig);
                }
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        });
    }
}
