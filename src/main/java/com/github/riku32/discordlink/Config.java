package com.github.riku32.discordlink;

import lombok.Data;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.NoSuchElementException;

/**
 * {@link com.github.riku32.discordlink.DiscordLink} configuration container
 */
@Data
public class Config {
    private final String token;
    private final String serverID;
    private final String ownerID;

    private final boolean chatEnabled;

    // Player formats
    private final boolean playerFormatEnabled;
    private final String playerFormatLinked;
    private final String playerFormatUnlinked;

    // Discord user format
    private final String discordFormatLinked;
    private final String discordFormatUnlinked;

    private final boolean crossChatEnabled;
    private final String channelID;
    private final String webhook;

    private final boolean channelBroadcastDeath;
    private final boolean channelBroadcastJoin;
    private final boolean channelBroadcastQuit;

    private final String kickNotInGuild;
    private final String kickBanned;
    private final String kickToS;

    private final boolean statusEnabled;

    private final String statusJoinLinked;
    private final String statusJoinUnlinked;

    private final String statusQuitLinked;
    private final String statusQuitUnlinked;

    private final String statusDeathLinked;
    private final String statusDeathUnlinked;

    private final boolean linkRequired;
    private final boolean verifySpawn;
    private final boolean allowUnlink;

    /**
     * Create a config object from {@link com.github.riku32.discordlink.DiscordLink} instance
     * @param configuration object
     * @throws NoSuchElementException if required values are missing
     */
    public Config(FileConfiguration configuration) throws NoSuchElementException {
        linkRequired = Boolean.parseBoolean(getAsStringNotNull(configuration, "link.required"));
        if (linkRequired) {
            verifySpawn = Boolean.parseBoolean(getAsStringNotNull(configuration, "link.verify_spawn"));
        } else {
            verifySpawn = false;
        }
        allowUnlink = Boolean.parseBoolean(getAsStringNotNull(configuration, "link.allow_unlink"));

        token = getAsStringNotNull(configuration, "discord.token");
        serverID = getAsStringNotNull(configuration, "discord.server_id");
        ownerID = getAsStringNotNull(configuration, "discord.owner_id");

        if (Boolean.parseBoolean(getAsStringNotNull(configuration, "chat.enabled"))) {
            chatEnabled = true;

            playerFormatEnabled = Boolean.parseBoolean(getAsStringNotNull(configuration, "chat.format.player.enabled"));

            playerFormatLinked = playerFormatEnabled ? getAsStringNotNull(configuration, "chat.format.player.linked") : null;
            discordFormatLinked = getAsStringNotNull(configuration, "chat.format.discord.linked");

            if (!linkRequired) {
                playerFormatUnlinked = playerFormatEnabled ? getAsStringNotNull(configuration, "chat.format.player.unlinked") : null;
                discordFormatUnlinked = getAsStringNotNull(configuration, "chat.format.discord.unlinked");
            } else {
                playerFormatUnlinked = null;
                discordFormatUnlinked = null;
            }

            if (Boolean.parseBoolean(getAsStringNotNull(configuration, "chat.crosschat.enabled"))) {
                crossChatEnabled = true;
                channelID = getAsStringNotNull(configuration, "chat.crosschat.channel_id");
                webhook = getAsStringNotNull(configuration, "chat.crosschat.webhook");

                channelBroadcastDeath = Boolean.parseBoolean(getAsStringNotNull(configuration, "chat.crosschat.events.death"));
                channelBroadcastJoin = Boolean.parseBoolean(getAsStringNotNull(configuration, "chat.crosschat.events.join"));
                channelBroadcastQuit = Boolean.parseBoolean(getAsStringNotNull(configuration, "chat.crosschat.events.quit"));
            } else {
                crossChatEnabled = false;
                channelID = null;
                webhook = null;
                channelBroadcastDeath = false;
                channelBroadcastJoin = false;
                channelBroadcastQuit = false;
            }
        } else {
            chatEnabled = false;
            crossChatEnabled = false;
            playerFormatEnabled = false;
            playerFormatLinked = null;
            playerFormatUnlinked = null;
            discordFormatLinked = null;
            discordFormatUnlinked = null;
            channelID = null;
            webhook = null;
            channelBroadcastDeath = false;
            channelBroadcastJoin = false;
            channelBroadcastQuit = false;
        }

        if (Boolean.parseBoolean(getAsStringNotNull(configuration, "status_messages.enabled"))) {
            statusEnabled = true;
            statusJoinLinked = getAsStringNotNull(configuration, "status_messages.join.linked");
            statusQuitLinked = getAsStringNotNull(configuration, "status_messages.quit.linked");
            statusDeathLinked = getAsStringNotNull(configuration, "status_messages.death.linked");

            if (!linkRequired) {
                statusJoinUnlinked = getAsStringNotNull(configuration, "status_messages.join.unlinked");
                statusQuitUnlinked = getAsStringNotNull(configuration, "status_messages.quit.unlinked");
                statusDeathUnlinked = getAsStringNotNull(configuration, "status_messages.death.unlinked");
            } else {
                statusJoinUnlinked = null;
                statusQuitUnlinked = null;
                statusDeathUnlinked = null;
            }
        } else {
            statusEnabled = false;
            statusJoinLinked = null;
            statusQuitLinked = null;
            statusDeathLinked = null;
            statusJoinUnlinked = null;
            statusQuitUnlinked = null;
            statusDeathUnlinked = null;
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
