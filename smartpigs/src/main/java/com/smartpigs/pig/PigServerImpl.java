package com.smartpigs.pig;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PigServerImpl extends UnicastRemoteObject implements PigServer {

    static final String NAME = PigServerImpl.class.getName();

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
}
