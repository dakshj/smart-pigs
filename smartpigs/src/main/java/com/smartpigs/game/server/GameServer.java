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

    /**
     * When a stone has been destroyed by either a collateral pig death, or by a bird attack,
     * control is transferred to {@link GameServer}, since that is the entity responsible for
     * selecting which other cell the stone will fall on.
     *
     * @param stoneOccupant The stone that needs to fall on another cell
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void stoneDestroyed(final Occupant stoneOccupant) throws RemoteException;
}
