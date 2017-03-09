package com.smartpigs.game.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartpigs.enums.OccupantType;
import com.smartpigs.exception.InvalidConfigurationException;
import com.smartpigs.model.Address;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Configuration;
import com.smartpigs.model.Grid;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ConfigurationReader {

    private final String configFilePath;

    /**
     * @param configFilePath The file path to read the configuration from
     */
    ConfigurationReader(final String configFilePath) {

        this.configFilePath = configFilePath;
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
     * @return The {@link Configuration} object read from the configuration file
     */
    Configuration readConfigurationFromFile() {
        final Gson gson = new Gson();

        JsonObject jsonObject = new JsonParser().parse(readFile(configFilePath)).getAsJsonObject();

        final Configuration configuration = gson.fromJson(jsonObject, Configuration.class);

        checkOccupantCapacityInGrid(configuration);

        buildPeerMap(configuration, jsonObject);

        initializeClosestPig(configuration, jsonObject);

        configuration.setGrid(getCreatedGrid(configuration));

        buildNeighborMap(configuration.getGrid(), configuration);

        validateConfiguration(configuration);

        return configuration;
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
                final Occupant occupant = grid.getOccupant(new Cell(row, col));

                if (occupant.getOccupantType() == OccupantType.PIG) {
                    final Occupant[][] neighbors = new Occupant[3][3];

                    // Set the other eight neighbors
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
            neighbors[row][col] = grid.getOccupant(new Cell(rowGrid, colGrid));
        } else {
            // If outside bounds, then set that neighbor as null
            neighbors[row][col] = null;
        }
    }

    /**
     * @param configuration The configuration to validate
     */
    private void validateConfiguration(final Configuration configuration) {
        if (configuration.getMaxHopCount() <= 0) {
            throw new InvalidConfigurationException("The max hop count is invalid!");
        }

        if (configuration.getAttackEta() < configuration.getHopDelay()) {
            throw new InvalidConfigurationException("The Attack ETA is too low!\n" +
                    "(It should be at least the same as the network's hop delay" +
                    " to convey at least one message.)");
        }

        if (configuration.getAttackedCell() == null) {
            throw new InvalidConfigurationException("The Attacked Cell is invalid!");
        }

        if (configuration.getAttackedCell().getRow() >= configuration.getRows() ||
                configuration.getAttackedCell().getCol() >= configuration.getColumns()) {
            throw new InvalidConfigurationException("The Attacked Cell is out of grid range!");
        }

        if (configuration.getGameServerAddress() == null) {
            throw new InvalidConfigurationException("The Game Server Address is invalid!");
        }

        if (configuration.getPigSet() == null || configuration.getPigSet().isEmpty()) {
            throw new InvalidConfigurationException("The Pig Set is invalid!");
        }

        if (configuration.getClosestPig() == null) {
            throw new InvalidConfigurationException("The pig closest to the bird launcher is invalid!");
        }

        if (configuration.getPeerMap() == null || configuration.getPeerMap().isEmpty()) {
            throw new InvalidConfigurationException("The Peer Map is invalid!\n"
                    + "(Please check the \"network\" JSON array)");
        }

        if (configuration.getNeighborMap() == null || configuration.getNeighborMap().isEmpty()) {
            throw new InvalidConfigurationException("The Neighbor Map is invalid!");
        }
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
            throw new InvalidConfigurationException("The number of occupants (" + occupantCount
                    + ")" + " exceeds the number of cells (" + cellCount + ").");
        }
    }
}
