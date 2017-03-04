package com.smartpigs.enums;

public enum ExecutionMode {

    GAME_SERVER,
    PIG_SERVER;

    public static ExecutionMode from(final int mode) {
        switch (mode) {
            case 0:
                return GAME_SERVER;

            case 1:
                return PIG_SERVER;
        }

        return null;
    }
}
