package com.smartpigs.exception;

import com.smartpigs.model.Configuration;

public class InvalidConfigurationException extends RuntimeException {

    /**
     * Thrown when any of the parameters of a {@link Configuration} are invalid.
     */
    public InvalidConfigurationException(final String message) {
        super(message);
    }
}
