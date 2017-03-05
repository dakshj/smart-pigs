package com.smartpigs.pig.client;

import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class NeighborCellUpdater {

    private final Pig neighbor;
    private final Pig originator;

    public NeighborCellUpdater(final Pig neighbor, final Pig originator) {
        this.neighbor = neighbor;
        this.originator = originator;
    }

    public void update() {
        try {
            PigServer.connect(originator).updateNeighborCell(neighbor);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
