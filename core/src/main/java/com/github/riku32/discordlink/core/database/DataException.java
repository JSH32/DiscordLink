package com.github.riku32.discordlink.core.database;

/**
 * Thrown when data cannot be retrieved from a data source
 */
public class DataException extends Exception {
    public DataException(String message) {
        super(message);
    }
}
