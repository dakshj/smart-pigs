package com.smartpigs.game.server;

import com.smartpigs.game.client.PigLocationsUpdater;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Configuration;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

class StoneController {

    private final Configuration configuration;

    StoneController(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Read {@link GameServer#stoneDestroyed(Occupant)}
     */
    void stoneDestroyed(final Occupant stoneOccupant) throws RemoteException {
        // Need to synchronously update all pigs' locations before toppling the stone over on its
        // neighbors, because pigs might have moved, and this stone might fall on the cell in which
        // a pig was *in the past*, but not anymore.
        new PigLocationsUpdater(getConfiguration().getPigSet(), getConfiguration().getGrid()).update();

        // Since stoneOccupant has fallen, we can set it as an EMPTY Occupant
        // in the configuration's grid
        getConfiguration().getGrid().getOccupants().stream()
                .flatMap(Collection::stream)
                .filter(occupant ->
                        occupant.getOccupiedCell().equals(stoneOccupant.getOccupiedCell()))
                .findFirst()
                .ifPresent(occupant ->
                        getConfiguration().getGrid().setOccupantAsEmpty(stoneOccupant.getOccupiedCell()));

        // Randomly select a Cell for stoneOccupant to fall on
        int row = stoneOccupant.getOccupiedCell().getRow()
                + ThreadLocalRandom.current().nextInt(-1, 2);
        int col = stoneOccupant.getOccupiedCell().getCol()
                + ThreadLocalRandom.current().nextInt(-1, 2);
        Cell stoneFallingCell = new Cell(row, col);

        while (stoneFallingCell.equals(stoneOccupant.getOccupiedCell()) ||
                row < 0 || row >= getConfiguration().getRows() ||
                col < 0 || col >= getConfiguration().getColumns()) {
            // Regenerate the random Cell since previous Cell was invalid
            row = stoneOccupant.getOccupiedCell().getRow()
                    + ThreadLocalRandom.current().nextInt(-1, 2);
            col = stoneOccupant.getOccupiedCell().getCol()
                    + ThreadLocalRandom.current().nextInt(-1, 2);
            stoneFallingCell = new Cell(row, col);
        }

        System.out.println("Stone at Cell " + stoneOccupant.getOccupiedCell() + " was destroyed.");

        final Cell finalStoneFallingCell = stoneFallingCell;
        getConfiguration().getGrid().getOccupants().stream()
                .flatMap(Collection::stream)
                .filter(occupant -> occupant.getOccupiedCell().equals(finalStoneFallingCell))
                .findFirst()
                .ifPresent(occupant -> {
                    switch (occupant.getOccupantType()) {
                        case PIG:
                            System.out.println("Stone falling on " + occupant
                                    + " at Cell " + occupant.getOccupiedCell() + ".");
                            try {
                                PigServer.connect(((Pig) occupant)).killed();
                            } catch (RemoteException | NotBoundException e) {
                                e.printStackTrace();
                            }
                            break;

                        case STONE:
                            System.out.println("Stone falling on another stone at Cell "
                                    + occupant.getOccupiedCell() + ".");
                            try {
                                stoneDestroyed(occupant);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            break;

                        case EMPTY:
                            System.out.println("Stone falling on an empty Cell at "
                                    + occupant.getOccupiedCell() + ".");
                            break;
                    }
                });
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}
