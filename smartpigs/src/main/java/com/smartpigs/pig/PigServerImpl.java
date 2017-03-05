package com.smartpigs.pig;

import com.smartpigs.exception.PigNotInitializedException;
import com.smartpigs.model.Address;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.client.BirdAttackInformer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class PigServerImpl extends UnicastRemoteObject implements PigServer {

    public static final String NAME = "Pig Server";

    private Pig pig;
    private int maxHopCount;
    private long hopDelay;
    private boolean floodedBirdApproaching = false;

    public PigServerImpl(final int portNo) throws RemoteException {
        try {
            startServer(portNo);
        } catch (RemoteException ignored) {
            System.out.println(NAME + "@" + portNo + " failed to start!");
        }
    }

    private void startServer(final int portNo) throws RemoteException {
        final Registry registry = LocateRegistry.createRegistry(portNo);
        registry.rebind(NAME, this);
    }

    @Override
    public void receiveData(final Pig pig, final int maxHopCount,
            final long hopDelay) throws RemoteException {
        setPig(pig);
        setMaxHopCount(maxHopCount);
        setHopDelay(hopDelay);
    }

    @Override
    public void birdLaunched(final long attackEta, final Cell attackedCell) throws RemoteException {
        if (pig == null) {
            throw new PigNotInitializedException();
        }

        new BirdAttackInformer(pig.getAddress(), new ArrayList<>(), pig.getNeighborAddresses(),
                attackEta, attackedCell, maxHopCount, hopDelay).inform();
    }

    @Override
    public void birdApproaching(final List<Address> path, final long attackEta,
            final Cell attackedCell, final int currentHopCount) throws RemoteException {
        if (currentHopCount == 0 || floodedBirdApproaching) {
            return;
        }

        floodedBirdApproaching = true;

        new BirdAttackInformer(pig.getAddress(), new ArrayList<>(path),
                pig.getNeighborAddresses(), attackEta, attackedCell,
                currentHopCount, hopDelay).inform();
    }

    private void setPig(final Pig pig) {
        this.pig = pig;
    }

    private void setMaxHopCount(final int maxHopCount) {
        this.maxHopCount = maxHopCount;
    }

    private void setHopDelay(final long hopDelay) {
        this.hopDelay = hopDelay;
    }
}
