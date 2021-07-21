package com.github.riku32.discordlink.core.commands;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.core.platform.PlatformPlugin;

import java.util.UUID;

public class CommandSender {
    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private static final String CONSOLE_NAME = "Console";

    private final PlatformPlayer player;
    private final PlatformPlugin plugin;

    /**
     * Create a sender
     *
     * @param player null if console
     */
    public CommandSender(PlatformPlayer player, PlatformPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    /**
     * Check if the sender was console
     *
     * @return boolean
     */
    public boolean isConsole() {
        return player == null;
    }

    /**
     * Get name of sender
     *
     * @return name
     */
    public String getName() {
        if (isConsole())
            return CONSOLE_NAME;

        return player.getName();
    }

    /**
     * Get UUID of sender
     *
     * @return UUID
     */
    public UUID getUniqueId() {
        if (isConsole())
            return CONSOLE_UUID;

        return player.getUuid();
    }

    /**
     * Send a message to the sender
     *
     * @param message to send
     */
    public void sendMessage(String message) {
        if (isConsole())
            plugin.log(message);
        else
            player.sendMessage(message);
    }
}
