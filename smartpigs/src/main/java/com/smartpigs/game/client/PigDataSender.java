package com.smartpigs.game.client;

import com.smartpigs.game.Configuration;

/**
 * Creates multiple Java RMI Client instances to send required data to all Pig Servers.
 * <p>
 * The sent data includes:
 * <li>Pig ID
 * <li>Set of Logical Neighbors
 * <li>Network's Hop Count
 */
public class PigDataSender {

    private final Configuration configuration;

    public PigDataSender(final Configuration configuration) {
        this.configuration = configuration;
    }

    public void send() {

    }
}
