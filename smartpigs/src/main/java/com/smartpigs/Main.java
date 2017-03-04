package com.smartpigs;

import com.smartpigs.enums.ExecutionMode;
import com.smartpigs.game.GameServerImpl;
import com.smartpigs.pig.PigServerImpl;

import java.rmi.RemoteException;

public class Main {

    /**
     * @param args <p>
     *             args[0]:
     *             <p>
     *             Use "0" to start a Game Server.
     *             <p>
     *             Use "1" to start a Pig Server.
     *             <p>
     *             <p>
     *             args[1]:
     *             <p>
     *             The port number to start the server on.
     *             <p>
     *             <p>
     *             args[2]: <i>(Only required when starting a Game Server)</i>
     *             <p>
     *             The file path of the configuration file.
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    public static void main(String[] args) throws RemoteException {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No command-line arguments provided." +
                    " Please refer the JavaDoc to know more on these arguments.");
        }

        ExecutionMode executionMode;

        try {
            executionMode = ExecutionMode.from(Integer.parseInt(args[0]));
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Execution Mode is invalid.");
        }

        assert executionMode != null;
        switch (executionMode) {
            case GAME_SERVER:
                new GameServerImpl(Integer.parseInt(args[1]), args[2]);
                break;

            case PIG_SERVER:
                new PigServerImpl(Integer.parseInt(args[1]));
                break;
        }
    }
}
