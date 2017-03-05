package com.smartpigs.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartpigs.enums.OccupantType;
import com.smartpigs.exception.ClosestPigNullException;
import com.smartpigs.exception.OccupantsExceedCellsException;
import com.smartpigs.game.client.BirdLauncher;
import com.smartpigs.game.client.PigDataSender;
import com.smartpigs.model.Address;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Grid;
import com.smartpigs.model.Occupant;
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
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        new PigDataSender(configuration).send();

        new BirdLauncher(configuration.getClosestPig(),
                configuration.getAttackEta(), configuration.getAttackedCell())
                .launch();
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

        mapPigIdsToPeerAddresses(configuration, jsonObject);

        initializeClosestPig(configuration, jsonObject);

        final Grid grid = createGrid(configuration);

        validateConfiguration(configuration);

        return configuration;
    }

    /**
     * Creates a random grid of pigs, Stones, and Empty cells
     * with the number of Pigs and Stones specified in the given configuration.
     * <p>
     * The rest of the cells are marked as empty.
     *
     * @param configuration The configuration to use while creating a random grid
     * @return The grid created using the given configuration
     */
    private Grid createGrid(final Configuration configuration) {
        final Occupant[][] occupants = new Occupant[configuration.getRows()][configuration.getColumns()];

        addOccupantsToGrid(configuration.getPigSet(), OccupantType.PIG, occupants,
                configuration.getRows(), configuration.getColumns());

        final Set<Occupant> stoneSet = IntStream.range(0, configuration.getNoOfStones())
                .boxed()
                .map(i -> new Occupant())
                .collect(Collectors.toSet());

        addOccupantsToGrid(stoneSet, OccupantType.STONE, occupants,
                configuration.getRows(), configuration.getColumns());

        setRemainingCellsAsEmpty(occupants);

        return new Grid(
                Arrays.stream(occupants)
                        .map(Arrays::asList)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Randomly adds occupants (Pigs and Stones) to the {@code occupants} grid.
     *
     * @param occupantSet  The {@link Set} of occupants to add
     * @param occupantType The type of occupant (Pig or Stone)
     * @param occupants    The grid of occupants
     * @param rows         The number of rows of the {@code occupants} grid
     * @param cols         The number of columns of the {@code occupants} grid
     */
    private void addOccupantsToGrid(final Set<? extends Occupant> occupantSet,
            final OccupantType occupantType, final Occupant[][] occupants,
            final int rows, final int cols) {
        for (final Occupant occupant : occupantSet) {
            int row = ThreadLocalRandom.current().nextInt(0, rows);
            int col = ThreadLocalRandom.current().nextInt(0, cols);

            // Validation to check if a to-be-assigned Cell is already occupied
            while (occupants[row][col] != null) {
                row = ThreadLocalRandom.current().nextInt(0, rows);
                col = ThreadLocalRandom.current().nextInt(0, cols);
            }

            occupant.setOccupiedCell(new Cell(row, col));
            occupant.setOccupantType(occupantType);

            occupants[row][col] = occupant;
        }
    }

    /**
     * Sets cells which have not been occupied by Pigs and Stones as {@code Empty}.
     *
     * @param occupants The grid of occupants in which to add empty cells
     */
    private void setRemainingCellsAsEmpty(final Occupant[][] occupants) {
        for (int row = 0; row < occupants.length; row++) {
            for (int col = 0; col < occupants[row].length; col++) {
                if (occupants[row][col] == null) {
                    final Occupant occupant = new Occupant();
                    occupant.setOccupantType(OccupantType.EMPTY);
                    occupants[row][col] = occupant;
                }
            }
        }
    }

    /**
     * Maps each Pig to its peers' addresses by resolving their IDs to
     * {@link Address} references.
     *
     * @param configuration The configuration to fetch pigs from
     * @param jsonObject    The configuration JSON Object to read peer IDs from
     */
    private void mapPigIdsToPeerAddresses(final Configuration configuration,
            final JsonObject jsonObject) {
        final JsonArray network = jsonObject.getAsJsonArray("network");

        network.forEach(element -> {
            final JsonObject object = element.getAsJsonObject();
            final Pig pig = configuration.getPigFromId(object.get("pig").getAsString());

            final JsonArray peers = object.get("peers").getAsJsonArray();

            peers.forEach(peerId -> {
                final Pig peer = configuration.getPigFromId(peerId.getAsString());
                pig.addPeerAddress(peer.getAddress());
            });
        });
    }

    /**
     * Sets a pig as the closest pig in the configuration.
     *
     * @param configuration The configuration within which to set the closest pig
     * @param jsonObject    The JSON data from which to read the closest pig's ID
     */
    private void initializeClosestPig(final Configuration configuration,
            final JsonObject jsonObject) {
        //noinspection OptionalGetWithoutIsPresent
        configuration.setClosestPig(
                configuration.getPigSet().stream()
                        .filter(pig ->
                                pig.getId().equals(jsonObject.get("closestPig").getAsString()))
                        .findFirst()
                        .get()
        );
    }

    /**
     * @param configuration The configuration to validate
     */
    private void validateConfiguration(final Configuration configuration) {
        checkOccupantCapacityInGrid(configuration);

        if (configuration.getClosestPig() == null) {
            throw new ClosestPigNullException();
        }

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
