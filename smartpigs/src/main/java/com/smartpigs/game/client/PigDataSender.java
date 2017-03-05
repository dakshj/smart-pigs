package com.smartpigs.game.client;

import com.smartpigs.model.Address;
import com.smartpigs.model.Configuration;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Creates multiple Java RMI Client instances to send required data to all Pig Servers.
 * <p>
 * The sent data includes:
 * <li>Pig ID
 * <li>Set of Peers' Addresses
 * <li>Network's Hop Count
 * <li>Network's Hop Delay
 */
public class PigDataSender {

    private final Configuration configuration;
    private final Address gameServerAddress;

    public PigDataSender(final Configuration configuration, final Address gameServerAddress) {
        this.configuration = configuration;
        this.gameServerAddress = gameServerAddress;
    }

    /**
     * Sends the required data to every {@link PigServer} by creating an RMI connection.
     */
    public void send() {
        configuration.getPigSet().forEach(this::send);
    }

    private void send(final Pig pig) {
        try {
            PigServer.connect(pig).receiveData(gameServerAddress, pig,
                    configuration.getFromPeerMap(pig), configuration.getFromNeighborMap(pig),
                    configuration.getMaxHopCount(), configuration.getHopDelay());
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
