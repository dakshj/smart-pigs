package com.smartpigs;

import com.smartpigs.enums.ExecutionMode;
import com.smartpigs.game.GameServerImpl;
import com.smartpigs.pig.PigServerImpl;

import java.rmi.RemoteException;

public class Main {

    public static void main(String[] args) throws RemoteException {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No arguments provided.");
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
