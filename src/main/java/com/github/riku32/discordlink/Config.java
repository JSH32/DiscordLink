package com.github.riku32.discordlink;

import lombok.Data;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.NoSuchElementException;

/**
 * {@link com.github.riku32.discordlink.DiscordLink} configuration container
 *
 */
@Data
public class Config {
    private final String token;
    private final String serverID;
    private final String ownerID;

    private final boolean chatEnabled;
    private final String playerFormat;
    private final String discordFormat;

    private final boolean crossChatEnabled;
    private final String channelID;
    private final String webhook;

    private final String kickNotInGuild;
    private final String kickBanned;
    private final String kickToS;

    private final boolean statusEnabled;
    private final String statusJoin;
    private final String statusQuit;
    private final String statusDeath;

    /**
     * Create a config object from {@link com.github.riku32.discordlink.DiscordLink} instance
     * @param configuration object
     * @throws NoSuchElementException if required values are missing
     */
    public Config(FileConfiguration configuration) throws NoSuchElementException {
        token = getAsStringNotNull(configuration, "discord.token");
        serverID = getAsStringNotNull(configuration, "discord.server_id");
        ownerID = getAsStringNotNull(configuration, "discord.owner_id");

        if (Boolean.parseBoolean(getAsStringNotNull(configuration, "chat.enabled"))) {
            chatEnabled = true;
            playerFormat = getAsStringNotNull(configuration, "chat.format.player");
            discordFormat = getAsStringNotNull(configuration, "chat.format.discord");

            if (Boolean.parseBoolean(getAsStringNotNull(configuration, "chat.crosschat.enabled"))) {
                crossChatEnabled = true;
                channelID = getAsStringNotNull(configuration, "chat.crosschat.channel_id");
                webhook = getAsStringNotNull(configuration, "chat.crosschat.webhook");
            } else {
                crossChatEnabled = false;
                channelID = null;
                webhook = null;
            }
        } else {
            chatEnabled = false;
            crossChatEnabled = false;
            playerFormat = null;
            discordFormat = null;
            channelID = null;
            webhook = null;
        }

        if (Boolean.parseBoolean(getAsStringNotNull(configuration, "status_messages.enabled"))) {
            statusEnabled = true;
            statusJoin = getAsStringNotNull(configuration, "status_messages.join");
            statusQuit = getAsStringNotNull(configuration, "status_messages.quit");
            statusDeath = getAsStringNotNull(configuration, "status_messages.death");
        } else {
            statusEnabled = false;
            statusJoin = null;
            statusQuit = null;
            statusDeath = null;
        }

        // Kick messages, cant be empty
        // TODO: Make this optional
        kickNotInGuild = getAsStringNotNull(configuration, "kick_messages.not_in_guild");
        kickBanned = getAsStringNotNull(configuration, "kick_messages.banned");
        kickToS = getAsStringNotNull(configuration, "kick_messages.tos");
    }

    private String getAsStringNotNull(FileConfiguration config, String path) throws NoSuchElementException {
        Object value = config.get(path);
        if (value == null)
            throw new NoSuchElementException(path + " was not found in the config");

        return String.valueOf(value);
    }
}
