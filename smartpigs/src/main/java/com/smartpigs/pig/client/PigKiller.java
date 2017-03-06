package com.smartpigs.pig.client;

import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class PigKiller {

    private final Pig pig;

    public PigKiller(final Pig pig) {
        this.pig = pig;
    }

    /**
     * Remotely connects to a pig so as to kill it.
     */
    public void kill() {
        try {
            PigServer.connect(pig).killed();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
