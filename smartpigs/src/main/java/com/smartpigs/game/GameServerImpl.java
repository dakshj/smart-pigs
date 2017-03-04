package com.smartpigs.game;

import com.google.gson.Gson;
import com.smartpigs.exception.OccupantsExceedCellsException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameServerImpl extends UnicastRemoteObject implements GameServer {

    private static final String NAME = GameServerImpl.class.getName();

    public GameServerImpl(final int portNo, final String configFilePath) throws RemoteException {
        try {
            startServer(portNo);
        } catch (RemoteException ignored) {
            System.out.println(NAME + "@" + portNo + " failed to start!");
        }

        final Configuration configuration = readConfigurationFromFile(configFilePath);

        // TODO initialize Pigs using configuration
        // TODO create grid using configuration
        // TODO assign neighbors to Pigs
    }

    private void startServer(final int portNo) throws RemoteException {
        final Registry registry = LocateRegistry.createRegistry(portNo);
        registry.rebind(NAME, this);
    }

    private Configuration readConfigurationFromFile(final String configFilePath) {
        final Gson gson = new Gson();

        final Configuration configuration =
                gson.fromJson(readFile(configFilePath), Configuration.class);

        validateConfiguration(configuration);

        return configuration;
    }

    /**
     * @param configuration The configuration to validate
     */
    private void validateConfiguration(final Configuration configuration) {
        checkOccupantCapacityInGrid(configuration);
    }

    /**
     * Checks whether the occupants (pigs + stones) are able to fit in the grid.
     *
     * @param configuration The configuration to check the grid capacity against
     */
    private void checkOccupantCapacityInGrid(final Configuration configuration) {
        final int occupantCount = configuration.getNoOfPigs() + configuration.getNoOfStones();
        final int cellCount = configuration.getRows() * configuration.getColumns();

        if (occupantCount > cellCount) {
            throw new OccupantsExceedCellsException(occupantCount, cellCount);
        }
    }

    private String readFile(final String configFilePath) {
        final StringBuilder builder = new StringBuilder();
        try {
            Files.lines(new File(configFilePath).toPath())
                    .forEach(builder::append);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }
}
