package com.smartpigs.game.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartpigs.enums.OccupantType;
import com.smartpigs.exception.ClosestPigNullException;
import com.smartpigs.exception.OccupantsExceedCellsException;
import com.smartpigs.game.client.BirdLauncher;
import com.smartpigs.game.client.PigDataSender;
import com.smartpigs.game.client.StatusRequester;
import com.smartpigs.model.Address;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Configuration;
import com.smartpigs.model.Grid;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameServerImpl extends UnicastRemoteObject implements GameServer {

    // TODO Maintain if a stone has been destroyed already or not, to prevent a random chain of one stone falling on another, and the other falling on the same initial stone, and so on...

    static final String NAME = "Game Server";

    private Configuration configuration;

    /**
     * Creates a Game Server, reads the {@link Configuration} from a
     * configuration file, initializes grids and grid occupants, and provides runtime information
     * to each of the Pig nodes running on their respective {@link PigServer}s.
     *
     * @param configFilePath The file path from which to read the configuration
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    public GameServerImpl(final String configFilePath) throws RemoteException {
        setConfiguration(readConfigurationFromFile(configFilePath));

        try {
            startServer(getConfiguration().getGameServerAddress().getPortNo());
        } catch (RemoteException ignored) {
            System.out.println(NAME + "@" + getConfiguration().getGameServerAddress().getPortNo()
                    + " failed to start!");
        }

        play(false);
    }

    private void play(final boolean replay) {
        if (replay) {
            System.out.println("\n\n" +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + "\n\n");
        }

        System.out.println("Welcome to Smart Pigs!\n\nI am your host, Game Server.\n");
        System.out.println("Grid :\n" + getConfiguration().getGrid() + "\n");

        System.out.println("Sending data to all pigs...");
        new PigDataSender(configuration, configuration.getGameServerAddress()).send();
        System.out.println("Data sent.\n");

        System.out.print("Launching a bird on Cell " + getConfiguration().getAttackedCell() + ".");
        System.out.println(" ETA : " + getConfiguration().getAttackEta() + " ms.");

        // If the attackedCell is a stone, then call stoneDestroyed() after waiting for attackEta
        // Else call birdLaunched() on the closest pig
        getConfiguration().getGrid().getOccupants().stream()
                .flatMap(Collection::stream)
                .filter(occupant ->
                        occupant.getOccupiedCell().equals(getConfiguration().getAttackedCell()))
                .findFirst()
                .ifPresent(occupant -> {
                    new BirdLauncher(configuration.getClosestPig(),
                            configuration.getAttackEta(), configuration.getAttackedCell(),
                            configuration.getMaxHopCount())
                            .launch();

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            switch (occupant.getOccupantType()) {
                                case EMPTY:
                                    System.out.println("Bird crashed on an empty Cell at "
                                            + occupant.getOccupiedCell() + ".");
                                    break;

                                case PIG:
                                    break;

                                case STONE:
                                    try {
                                        stoneDestroyed(occupant);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }

                            gameOver();
                        }
                    }, getConfiguration().getAttackEta());
                });
    }

    private void gameOver() {
        System.out.println("\nGame Over!");

        score(new StatusRequester(configuration.getPigSet()).request());

        askToPlayAgain();
    }

    /**
     * @param hitMap {@link Map} of Pig IDs vs. their hit status
     */
    private void score(final Map<String, Boolean> hitMap) {
        int hitCount = 0;

        for (final Map.Entry<String, Boolean> entry : hitMap.entrySet()) {
            if (entry.getValue()) {
                System.out.println("Pig " + entry.getKey() + " was hit!");
                hitCount++;
            } else {
                System.out.println("Pig " + entry.getKey() + " was not hit!");
            }
        }

        System.out.println("\nFinal Score : " + hitCount + " pigs hit!");
    }

    private void askToPlayAgain() {
        System.out.println("\nPlay Again? (y/n)");
        final String input = new Scanner(System.in).next();

        boolean playAgain = input.equalsIgnoreCase("y")
                || input.equalsIgnoreCase("yes");

        if (playAgain) {
            play(true);
        } else {
            System.out.println("\nThank you for playing Smart Pigs!\n\nI was your host, Game Server.");
            System.exit(0);
        }
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
     * <p>
     * Creates the grid with random pigs, stones and empty cells.
     * <p>
     * Additionally builds the peer and neighbor maps by using the provided configuration.
     *
     * @param configFilePath The file path to read the configuration from
     * @return The {@link Configuration} object read from the configuration file
     */
    private Configuration readConfigurationFromFile(final String configFilePath) {
        final Gson gson = new Gson();

        JsonObject jsonObject = new JsonParser().parse(readFile(configFilePath)).getAsJsonObject();

        final Configuration configuration = gson.fromJson(jsonObject, Configuration.class);

        buildPeerMap(configuration, jsonObject);

        initializeClosestPig(configuration, jsonObject);

        configuration.setGrid(getCreatedGrid(configuration));

        buildNeighborMap(configuration.getGrid(), configuration);

        validateConfiguration(configuration);

        return configuration;
    }

    /**
     * Maps each pig to its physical neighbors (i.e., grid occupants which are
     * one step away in all directions, including diagonals).
     * <p>
     * Stores the built neighbor map into {@link Configuration#neighborMap}.
     *
     * @param grid          The grid using which to build the neighbor map
     * @param configuration The configuration to put the neighbor map into
     */
    private void buildNeighborMap(final Grid grid, final Configuration configuration) {
        for (int row = 0; row < grid.getOccupants().size(); row++) {
            for (int col = 0; col < grid.getOccupants().get(row).size(); col++) {
                final Occupant occupant = grid.getOccupants().get(row).get(col);

                if (occupant.getOccupantType() == OccupantType.PIG) {
                    final Occupant[][] neighbors = new Occupant[3][3];
                    setNeighbor(neighbors, configuration, grid, row - 1, col - 1, 0, 0);
                    setNeighbor(neighbors, configuration, grid, row - 1, col, 0, 1);
                    setNeighbor(neighbors, configuration, grid, row - 1, col + 1, 0, 2);
                    setNeighbor(neighbors, configuration, grid, row, col - 1, 1, 0);
                    setNeighbor(neighbors, configuration, grid, row, col + 1, 1, 2);
                    setNeighbor(neighbors, configuration, grid, row + 1, col - 1, 2, 0);
                    setNeighbor(neighbors, configuration, grid, row + 1, col, 2, 1);
                    setNeighbor(neighbors, configuration, grid, row + 1, col + 1, 2, 2);

                    configuration.putInNeighborMap((Pig) occupant,
                            Arrays.stream(neighbors)
                                    .map(Arrays::asList)
                                    .collect(Collectors.toList())
                    );
                }
            }
        }
    }

    /**
     * Adds neighbors of a pig into a smaller 3x3 matrix, with the pig at the center.
     * <p>
     * If a cell is out of grid bounds, then it is stored as {@code null} within the smaller matrix.
     *
     * @param neighbors     The 3x3 matrix within which to add neighbors (pigs, stones, or empty cells)
     * @param configuration The configuration to read the grid dimensions from
     * @param grid          The grid to get occupants from so as to add them to {@code neighbors}
     * @param rowGrid       The row index within the original grid
     * @param colGrid       The column index within the original grid
     * @param row           The row index within the smaller 3x3 matrix, at which to store a neighbor
     * @param col           The column index within the smaller 3x3 matrix, at which to store a neighbor
     */
    private void setNeighbor(final Occupant[][] neighbors, final Configuration configuration,
            final Grid grid, final int rowGrid, final int colGrid, final int row, final int col) {
        // Validate that the given rowGrid and colGrid are within the grid bounds
        if (rowGrid >= 0 && rowGrid < configuration.getRows() &&
                colGrid >= 0 && colGrid < configuration.getColumns()) {
            neighbors[row][col] = grid.getOccupants().get(rowGrid).get(colGrid);
        } else {
            // If outside bounds, then set that neighbor as null
            neighbors[row][col] = null;
        }
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
    private Grid getCreatedGrid(final Configuration configuration) {
        final Occupant[][] occupants = new Occupant[configuration.getRows()][configuration.getColumns()];

        addOccupantsToGrid(configuration.getPigSet(), OccupantType.PIG, occupants,
                configuration.getRows(), configuration.getColumns());

        // Build a Set of Stone Occupants so as to re-use addOccupantsToGrid() for also adding stones
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

            // TODO Remove hard-coding of P1 at {3,4}
            if (occupant.getOccupantType() == OccupantType.PIG && ((Pig) occupant).getId().equals("1")) {
                occupants[row][col] = null;
                occupant.setOccupiedCell(new Cell(3, 4));
                occupants[3][4] = occupant;
            }
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
                    occupant.setOccupiedCell(new Cell(row, col));
                    occupant.setOccupantType(OccupantType.EMPTY);
                    occupants[row][col] = occupant;
                }
            }
        }
    }

    /**
     * Maps each Pig to its peers' addresses by resolving their IDs to
     * {@link Address} references.
     * <p>
     * Stores the built peer map into {@link Configuration#peerMap}.
     *
     * @param configuration The configuration to fetch pigs from
     * @param jsonObject    The configuration JSON Object to read peer IDs froms
     */
    private void buildPeerMap(final Configuration configuration,
            final JsonObject jsonObject) {
        final JsonArray network = jsonObject.getAsJsonArray("network");

        network.forEach(element -> {
            final JsonObject object = element.getAsJsonObject();
            final Pig pig = configuration.getPigFromId(object.get("pig").getAsString());

            final JsonArray peersJsonArray = object.get("peers").getAsJsonArray();

            final Set<Pig> peers = new HashSet<>();

            peersJsonArray.forEach(peerId -> {
                final Pig peer = configuration.getPigFromId(peerId.getAsString());

                peers.add(peer);
            });

            configuration.putInPeerMap(pig, peers);
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

    @Override
    public void stoneDestroyed(final Occupant stoneOccupant) throws RemoteException {
        // TODO Suppose a pig has an imminent bird attack, and says takeShelter to a neighbor,
        // and then the neighbor was able to move, and called the move() listener on to a pig,
        // (write more)

        // TODO Basically get all updated positions of all pigs before finding a position
        // for this stone to topple onto

        // Since stoneOccupant has fallen, we can set it as an EMPTY Occupant
        // in the configuration's grid
        getConfiguration().getGrid().getOccupants().stream()
                .flatMap(Collection::stream)
                .filter(occupant ->
                        occupant.getOccupiedCell().equals(stoneOccupant.getOccupiedCell()))
                .findFirst()
                .ifPresent(occupant -> occupant.setOccupantType(OccupantType.EMPTY));

        // Randomly select a Cell for stoneOccupant to fall on
        int row = stoneOccupant.getOccupiedCell().getRow()
                + ThreadLocalRandom.current().nextInt(-1, 2);
        int col = stoneOccupant.getOccupiedCell().getCol()
                + ThreadLocalRandom.current().nextInt(-1, 2);

        final Cell stoneFallingCell = new Cell(row, col);

        if (stoneFallingCell.equals(stoneOccupant.getOccupiedCell()) ||
                row < 0 || row >= getConfiguration().getRows() ||
                col < 0 || col >= getConfiguration().getColumns()) {
            // Regenerate the random Cell since previous Cell was invalid
            stoneDestroyed(stoneOccupant);
            return;
        }

        System.out.println("Stone at Cell " + stoneOccupant.getOccupiedCell() + " was destroyed.");

        getConfiguration().getGrid().getOccupants().stream()
                .flatMap(Collection::stream)
                .filter(occupant -> occupant.getOccupiedCell().equals(stoneFallingCell))
                .findFirst()
                .ifPresent(occupant -> {
                    switch (occupant.getOccupantType()) {
                        case PIG:
                            System.out.println("Stone falling on " + occupant
                                    + " at Cell " + occupant.getOccupiedCell() + ".");
                            try {
                                PigServer.connect(((Pig) occupant)).killedByFallingOver();
                            } catch (RemoteException | NotBoundException e) {
                                e.printStackTrace();
                            }
                            break;

                        case STONE:
                            System.out.println("Stone falling on another stone at Cell "
                                    + occupant.getOccupiedCell() + ".");
                            try {
                                stoneDestroyed(occupant);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            break;

                        case EMPTY:
                            System.out.println("Stone falling on an empty Cell at "
                                    + occupant.getOccupiedCell() + ".");
                            break;
                    }
                });
    }

    private Configuration getConfiguration() {
        return configuration;
    }

    private void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }
}
