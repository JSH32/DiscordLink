package com.github.riku32.discordlink;

import java.io.File;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class Database {
    private Connection connection;

    public Database(File dataFolder) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "database.db"));
        Statement statement = connection.createStatement();

        // Users
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (uuid TEXT NOT NULL, discord_id TEXT NOT NULL, verified INTEGER NOT NULL DEFAULT 0, message_id TEXT)");
        statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS users_discord_id_uindex ON users (discord_id)");
        statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS users_uuid_uindex ON users (uuid)");

        statement.close();
    }

    public Optional<PlayerInfo> getPlayerInfo(UUID uuid) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT discord_id, verified, uuid FROM users WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet resultSet = statement.executeQuery();

        Optional<PlayerInfo> result = Optional.empty();
        if (resultSet.next())
            result = Optional.of(new PlayerInfo(
                    resultSet.getString("discord_id"),
                    UUID.fromString(resultSet.getString("uuid")),
                    resultSet.getBoolean("verified")));

        resultSet.close();
        statement.close();

        return result;
    }

    public Optional<PlayerInfo> getPlayerInfo(String discordID) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT discord_id, verified, uuid FROM users WHERE discord_id = ?");
        statement.setString(1, discordID);
        ResultSet resultSet = statement.executeQuery();

        Optional<PlayerInfo> result = Optional.empty();
        if (resultSet.next())
            result = Optional.of(new PlayerInfo(
                    resultSet.getString("discord_id"),
                    UUID.fromString(resultSet.getString("uuid")),
                    resultSet.getBoolean("verified")));

        resultSet.close();
        statement.close();

        return result;
    }

    public void createPlayer(UUID uuid, String discordID, String messageID) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO users (uuid, discord_id, message_id) VALUES (?, ?, ?)");
        statement.setString(1, uuid.toString());
        statement.setString(2, discordID);
        statement.setString(3, messageID);
        statement.execute();

        statement.close();
    }

    public void verifyPlayer(String discordID) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE users SET verified = 1 WHERE discord_id = ?");
        statement.setString(1, discordID);
        statement.execute();

        statement.close();
    }

    public void deletePlayer(UUID uuid) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        statement.execute();
        statement.close();
    }

    public void deletePlayer(String discordID) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE discord_id = ?");
        statement.setString(1, discordID);
        statement.execute();
        statement.close();
    }

    public boolean isDiscordLinked(String discordID) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM users WHERE discord_id = ?)");
        statement.setString(1, discordID);
        ResultSet resultSet = statement.executeQuery();

        boolean exist = false;
        if (resultSet.next())
            exist = resultSet.getBoolean(1);

        resultSet.close();
        statement.close();

        return exist;
    }

    public String getMessageId(String discordID) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT message_id FROM users WHERE discord_id = ?");
        statement.setString(1, discordID);
        ResultSet resultSet = statement.executeQuery();

        String messageID = null;
        if (resultSet.next())
            messageID = resultSet.getString(1);

        resultSet.close();
        statement.close();

        return messageID;
    }
}
