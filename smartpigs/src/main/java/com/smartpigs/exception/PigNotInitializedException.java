package com.smartpigs.exception;

public class PigNotInitializedException extends RuntimeException {

    public PigNotInitializedException() {
        super("Pig is not initialized!");
    }
}
