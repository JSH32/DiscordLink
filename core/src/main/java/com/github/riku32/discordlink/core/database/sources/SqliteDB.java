package com.github.riku32.discordlink.core.database.sources;

import com.github.riku32.discordlink.core.database.DataException;
import com.github.riku32.discordlink.core.database.managers.PlayerManager;
import com.github.riku32.discordlink.core.database.model.PlayerIdentity;
import com.github.riku32.discordlink.core.database.model.PlayerInfo;

import java.io.File;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SqliteDB implements PlayerManager {
    private final Connection connection;

    public SqliteDB(File dataFolder) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "database.db"));
        Statement statement = connection.createStatement();

        // Users
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (uuid TEXT NOT NULL, discord_id TEXT NOT NULL, verified INTEGER NOT NULL DEFAULT 0, message_id TEXT)");
        statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS users_discord_id_uindex ON users (discord_id)");
        statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS users_uuid_uindex ON users (uuid)");

        statement.close();
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {}
    }

    @Override
    public Optional<PlayerInfo> getPlayerInfo(PlayerIdentity playerIdentity) throws DataException {
        try {
            PreparedStatement statement;
            if (playerIdentity.isUuid()) {
                statement = connection.prepareStatement("SELECT discord_id, verified, uuid FROM users WHERE uuid = ?");
                statement.setString(1, playerIdentity.getValue().toString());
            } else {
                statement = connection.prepareStatement("SELECT discord_id, verified, uuid FROM users WHERE discord_id = ?");
                statement.setString(1, (String) playerIdentity.getValue());
            }

            ResultSet resultSet = statement.executeQuery();

            Optional<PlayerInfo> result;

            if (resultSet.next()) {
                result = Optional.of(new PlayerInfo(
                        resultSet.getString("discord_id"),
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getBoolean("verified")));
            } else {
                result = Optional.empty();
            }

            resultSet.close();
            statement.close();

            return result;
        } catch (SQLException e) {
            throw new DataException(e.toString());
        }
    }

    public void createPlayer(UUID uuid, String discordID, String messageID) throws DataException {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (uuid, discord_id, message_id) VALUES (?, ?, ?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, discordID);
            statement.setString(3, messageID);
            statement.execute();

            statement.close();
        } catch (SQLException e) {
            throw new DataException(e.toString());
        }
    }

    @Override
    public void deletePlayer(PlayerIdentity playerIdentity) throws DataException {
        try {
            PreparedStatement statement;
            if (playerIdentity.isUuid()) {
                statement = connection.prepareStatement("DELETE FROM users WHERE uuid = ?");
                statement.setString(1, playerIdentity.getValue().toString());
            } else {
                statement = connection.prepareStatement("DELETE FROM users WHERE discord_id = ?");
                statement.setString(1, (String) playerIdentity.getValue());
            }

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            throw new DataException(e.toString());
        }
    }

    @Override
    public void verifyPlayer(PlayerIdentity playerIdentity) throws DataException {
        try {
            PreparedStatement statement;
            if (playerIdentity.isUuid()) {
                statement = connection.prepareStatement("UPDATE users SET verified = 1 WHERE uuid = ?");
                statement.setString(1, playerIdentity.getValue().toString());
            } else {
                statement = connection.prepareStatement("UPDATE users SET verified = 1 WHERE discord_id = ?");
                statement.setString(1, (String) playerIdentity.getValue());
            }

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            throw new DataException(e.toString());
        }
    }

    @Override
    public boolean isPlayerLinked(PlayerIdentity playerIdentity) throws DataException {
        try {
            PreparedStatement statement;
            if (playerIdentity.isUuid()) {
                statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM users WHERE uuid = ?)");
                statement.setString(1, playerIdentity.getValue().toString());
            } else {
                statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM users WHERE discord_id = ?)");
                statement.setString(1, (String) playerIdentity.getValue());
            }

            ResultSet resultSet = statement.executeQuery();
            boolean exist = false;
            if (resultSet.next())
                exist = resultSet.getBoolean(1);

            resultSet.close();
            statement.close();

            return exist;
        } catch (SQLException e) {
            throw new DataException(e.toString());
        }
    }

    @Override
    public String getVerificationMessage(PlayerIdentity playerIdentity) throws DataException {
        try {
            PreparedStatement statement;
            if (playerIdentity.isUuid()) {
                statement = connection.prepareStatement("SELECT message_id FROM users WHERE uuid = ?");
                statement.setString(1, playerIdentity.getValue().toString());
            } else {
                statement = connection.prepareStatement("SELECT message_id FROM users WHERE discord_id = ?");
                statement.setString(1, (String) playerIdentity.getValue());
            }

            ResultSet resultSet = statement.executeQuery();
            String messageID = null;
            if (resultSet.next())
                messageID = resultSet.getString(1);

            resultSet.close();
            statement.close();

            return messageID;
        } catch (SQLException e) {
            throw new DataException(e.toString());
        }
    }
}
