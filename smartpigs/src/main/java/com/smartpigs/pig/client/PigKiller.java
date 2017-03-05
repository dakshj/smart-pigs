package com.smartpigs.pig.client;

import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class PigKiller {

    private final Pig sender;

    public PigKiller(final Pig sender) {
        this.sender = sender;
    }

    public void kill() {
        try {
            PigServer.connect(sender).killByFallingOver();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
