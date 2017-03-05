package com.smartpigs.game.client;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.PigServer;
import com.smartpigs.pig.PigServerImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

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

    public void launch() {
        try {
            final Registry registry = LocateRegistry.getRegistry(closestPig.getAddress().getHost(),
                    closestPig.getAddress().getPortNo());
            PigServer pigServer = (PigServer) registry.lookup(PigServerImpl.NAME);

            pigServer.birdApproaching(new ArrayList<>(), attackEta, attackedCell, maxHopCount);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
