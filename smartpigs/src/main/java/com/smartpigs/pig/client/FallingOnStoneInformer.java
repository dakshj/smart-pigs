package com.smartpigs.pig.client;

import com.smartpigs.game.server.GameServer;
import com.smartpigs.model.Address;
import com.smartpigs.model.Occupant;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class FallingOnStoneInformer {

    private final Address gameServerAddress;
    private final Occupant stoneOccupant;

    public FallingOnStoneInformer(final Address gameServerAddress, final Occupant stoneOccupant) {
        this.gameServerAddress = gameServerAddress;
        this.stoneOccupant = stoneOccupant;
    }

    public void inform() {
        try {
            GameServer.connect(gameServerAddress).stoneDestroyed(stoneOccupant);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
