package com.smartpigs.game.server;

import com.smartpigs.model.Address;
import com.smartpigs.model.Occupant;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public interface GameServer extends Remote {

    /**
     * Establishes a connection with a {@link GameServer}.
     *
     * @param gameServerAddress The address of the {@link GameServer}
     * @return An instance of the connected {@link GameServer}, connected remotely via Java RMI
     * @throws RemoteException   Thrown when a Java RMI exception occurs
     * @throws NotBoundException Thrown when the remote binding does not exist in the {@link Registry}
     */
    static GameServer connect(final Address gameServerAddress)
            throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.getRegistry(gameServerAddress.getHost(),
                gameServerAddress.getPortNo());
        return (GameServer) registry.lookup(GameServerImpl.NAME);
    }

    void stoneDestroyed(Occupant stoneOccupant) throws RemoteException;
}
