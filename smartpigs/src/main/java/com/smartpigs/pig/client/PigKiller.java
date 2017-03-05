package com.smartpigs.pig.client;

import com.smartpigs.model.Pig;
import com.smartpigs.pig.PigServer;
import com.smartpigs.pig.PigServerImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PigKiller {

    private final Pig sender;

    public PigKiller(final Pig sender) {
        this.sender = sender;
    }

    public void kill() {
        try {
            final Registry registry = LocateRegistry.getRegistry(sender.getAddress().getHost(),
                    sender.getAddress().getPortNo());
            PigServer pigServer = (PigServer) registry.lookup(PigServerImpl.NAME);

            pigServer.killByFallingOver();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
