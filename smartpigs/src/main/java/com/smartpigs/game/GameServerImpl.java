package com.smartpigs.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartpigs.exception.OccupantsExceedCellsException;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.PigServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameServerImpl extends UnicastRemoteObject implements GameServer {

    private static final String NAME = "Game Server";

    /**
     * Creates a Game Server, reads the {@link Configuration} from a
     * configuration file, initializes grids and grid occupants, and provides runtime information
     * to each of the Pig nodes running on their respective {@link PigServer}s.
     *
     * @param portNo         The port number to start the Game Server on
     * @param configFilePath The file path from which to read the configuration
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
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

    /**
     * Starts the game server on the provided port number.
     * <p>
     * Uses {@value #NAME} as the name to associate with the remote reference.
     *
     * @param portNo The port number to start the game server on
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    private void startServer(final int portNo) throws RemoteException {
        final Registry registry = LocateRegistry.createRegistry(portNo);
        registry.rebind(NAME, this);
    }

    /**
     * Reads the configuration file and converts it into a {@link Configuration} object.
     * <p>
     * Uses {@link Gson} for mapping the JSON contents to the object variables.
     *
     * @param configFilePath The file path to read the configuration from
     * @return The {@link Configuration} object read from the configuration file
     */
    private Configuration readConfigurationFromFile(final String configFilePath) {
        final Gson gson = new Gson();

        JsonObject jsonObject = new JsonParser().parse(readFile(configFilePath)).getAsJsonObject();

        final Configuration configuration = gson.fromJson(jsonObject, Configuration.class);

        mapPigIdsToObjects(configuration, jsonObject);

        validateConfiguration(configuration);

        return configuration;
    }

    /**
     * Maps each Pig to its neighbors by resolving their IDs to {@link Pig} object references.
     *
     * @param configuration The configuration to fetch pigs from
     * @param jsonObject    The configuration JSON Object to read neighbor IDs from
     */
    private void mapPigIdsToObjects(final Configuration configuration, final JsonObject jsonObject) {
        final JsonArray network = jsonObject.getAsJsonArray("network");

        network.forEach(element -> {
            final JsonObject object = element.getAsJsonObject();
            final Pig pig = configuration.getPigFromId(object.get("pig").getAsString());

            final JsonArray logicalNeighbors = object.get("logicalNeighbors").getAsJsonArray();

            logicalNeighbors.forEach(neighborId -> {
                final Pig neighbor = configuration.getPigFromId(neighborId.getAsString());
                pig.addLogicalNeighbor(neighbor);
            });
        });
    }

    /**
     * @param configuration The configuration to validate
     */
    private void validateConfiguration(final Configuration configuration) {
        checkOccupantCapacityInGrid(configuration);
        // TODO add more Configuration validations
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

    /**
     * Reads the configuration JSON text from the given file path using the
     * Java 8 {@link Files#lines(Path)} API.
     *
     * @param configFilePath The file path to read the configuration from
     * @return The configuration JSON text which was read from the configuration file
     */
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
