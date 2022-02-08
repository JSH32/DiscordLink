package com.github.riku32.discordlink.core.config;

import io.ebean.annotation.Platform;

import java.io.File;

public class DatabaseSettings {
    public final Platform platform;
    public final String address;
    public final String databaseName;
    public final String username;
    public final String password;

    public DatabaseSettings(String platform, String address, String databaseName, String username, String password) {
        this.address = address;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;

        switch (platform.toLowerCase()) {
            case "h2": {
                this.platform = Platform.H2;
                break;
            }
            case "postgresql": {
                this.platform = Platform.POSTGRES;
                break;
            }
            case "mysql": {
                this.platform = Platform.MYSQL;
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format(
                        "Database type %s was not a valid database type\nAvailable options are: (H2, MySQL, PostgreSQL)",
                        platform));
            }
        }
    }

    /**
     * Generate connection URI for platform type
     *
     * @param folder location to store the file in
     * @param fileName name of the file to use for database, do not include extension
     * @return Connection URI
     */
    public String getConnectionUri(File folder, String fileName) {
        switch (platform) {
            case H2:
                return "jdbc:h2:file:" + new File(folder, fileName).getAbsolutePath();
            case MYSQL:
                return String.format("jdbc:mysql://%s/%s", address, databaseName);
            case POSTGRES:
                return String.format("jdbc:postgresql://%s/%s", address, databaseName);
            default:
                // This should never even happen
                return null;
        }
    }
}
