package com.smartpigs.game.server;

import com.smartpigs.game.client.BirdLauncher;
import com.smartpigs.game.client.PigDataSender;
import com.smartpigs.game.client.StatusRequester;
import com.smartpigs.model.Configuration;
import com.smartpigs.model.Occupant;
import com.smartpigs.pig.server.PigServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class GameServerImpl extends UnicastRemoteObject implements GameServer {

    static final String NAME = "Game Server";

    private final ConfigurationReader configurationReader;
    private final StoneController stoneController;

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
        configurationReader = new ConfigurationReader(configFilePath);
        setConfiguration(configurationReader.readConfigurationFromFile());

        stoneController = new StoneController(getConfiguration());

        try {
            startServer(getConfiguration().getGameServerAddress().getPortNo());
        } catch (RemoteException ignored) {
            System.out.println(NAME + "@" + getConfiguration().getGameServerAddress().getPortNo()
                    + " failed to start!");
        }

        play(false);
    }

    /**
     * Plays the game after starting the Game Server and reading the Configuration from
     * the configuration file.
     * <p>
     * Additionally, replays the game if the user chooses to do so.
     *
     * @param replay Flag to check if the call to this method is a first-time play or a replay
     *               <p>
     *               (Used for printing a divider between the previous game's output and
     *               this game's output)
     */
    private void play(final boolean replay) {
        // Regenerate the grid and re-read the configuration JSON file
        setConfiguration(configurationReader.readConfigurationFromFile());

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

        System.out.print("\nFinal Score : " + hitCount);

        if (hitCount == 1) {
            System.out.println(" pig hit!");
        } else {
            System.out.println(" pigs hit!");
        }
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
            System.exit(1);
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
     * Passes stone destruction handling to {@link StoneController}.
     *
     * @param stoneOccupant The stone that needs to fall on another cell
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    @Override
    public void stoneDestroyed(final Occupant stoneOccupant) throws RemoteException {
        stoneController.stoneDestroyed(stoneOccupant);
    }

    private Configuration getConfiguration() {
        return configuration;
    }

    private void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }
}
