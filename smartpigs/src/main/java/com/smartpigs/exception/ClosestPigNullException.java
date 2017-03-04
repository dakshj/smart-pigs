package com.smartpigs.exception;

/**
 * Thrown when the closest pig to the bird launcher is null.
 */
public class ClosestPigNullException extends RuntimeException {

    public ClosestPigNullException() {
        super("The closest pig to the bird launcher is invalid!");
    }
}
