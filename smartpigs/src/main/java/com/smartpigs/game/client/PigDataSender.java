package com.smartpigs.game.client;

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

    public PigDataSender(final Configuration configuration) {
        this.configuration = configuration;
    }

    public void send() {
        configuration.getPigSet().forEach(this::sendPigData);
    }

    /**
     * Creates an RMI connection to every {@link PigServer} and sends the required data.
     *
     * @param pig The pig to connect to
     */
    private void sendPigData(final Pig pig) {
        try {
            PigServer.connect(pig).receiveData(pig, configuration.getFromPeerMap(pig),
                    configuration.getFromNeighborMap(pig), configuration.getMaxHopCount(),
                    configuration.getHopDelay());
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
