package com.smartpigs.pig;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PigServerImpl extends UnicastRemoteObject implements PigServer {

    public static final String NAME = "Pig Server";

    private Pig pig;
    private int hopCount;
    private long hopDelay;

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
    public void receiveData(final Pig pig, final int hopCount,
            final long hopDelay) throws RemoteException {
        setPig(pig);
        setHopCount(hopCount);
        setHopDelay(hopDelay);
    }

    @Override
    public void birdLaunched(final long attackEta, final Cell attackedCell) throws RemoteException {
    }

    private void setPig(final Pig pig) {
        this.pig = pig;
    }

    private void setHopCount(final int hopCount) {
        this.hopCount = hopCount;
    }

    private void setHopDelay(final long hopDelay) {
        this.hopDelay = hopDelay;
    }
}
