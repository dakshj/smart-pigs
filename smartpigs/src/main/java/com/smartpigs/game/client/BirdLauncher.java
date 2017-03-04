package com.smartpigs.game.client;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.PigServer;
import com.smartpigs.pig.PigServerImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BirdLauncher {

    private final Pig closestPig;
    private final long attackEta;
    private final Cell attackedCell;

    public BirdLauncher(final Pig closestPig, final long attackEta, final Cell attackedCell) {
        this.closestPig = closestPig;
        this.attackEta = attackEta;
        this.attackedCell = attackedCell;
    }

    public void launch() {
        try {
            final Registry registry = LocateRegistry.getRegistry(closestPig.getAddress().getHost(),
                    closestPig.getAddress().getPortNo());
            PigServer pigServer = (PigServer) registry.lookup(PigServerImpl.NAME);

            pigServer.birdLaunched(attackEta, attackedCell);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
