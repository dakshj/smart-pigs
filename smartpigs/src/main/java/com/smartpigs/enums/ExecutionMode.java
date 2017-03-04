package com.smartpigs.enums;

public enum ExecutionMode {

    /**
     * Indicates that a Game Server needs to be started.
     */
    GAME_SERVER,

    /**
     * Indicates that a Pig Server needs to be started.
     */
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
