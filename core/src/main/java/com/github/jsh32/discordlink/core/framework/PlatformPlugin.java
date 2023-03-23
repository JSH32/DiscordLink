package com.github.jsh32.discordlink.core.framework;

import com.github.jsh32.discordlink.core.framework.command.CompiledCommand;
import com.github.jsh32.discordlink.core.framework.eventbus.EventBus;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Platform agnostic plugin implementation
 */
public interface PlatformPlugin {
    /**
     * Get a player from their UUID
     *
     * @param uuid target uuid
     * @return player
     */
    PlatformPlayer getPlayer(UUID uuid);

    /**
     * Get a player from their UUID
     *
     * @param uuid target uuid
     * @return player
     */
    PlatformOfflinePlayer getOfflinePlayer(UUID uuid);

    /**
     * Get a player from their username
     *
     * @param username target username
     * @return player
     */
    PlatformPlayer getPlayer(String username);

    /**
     * Get all players on the server
     *
     * @return player set
     */
    Set<PlatformPlayer> getPlayers();

    /**
     * Get the logger
     */
    Logger getLogger();

    /**
     * Get data directory
     */
    File getDataDirectory();

    /**
     * Disable the plugin
     */
    void disable();

    /**
     * Get the EventBus for events
     *
     * @return eventbus
     */
    EventBus getEventBus();

    /**
     * Register commands on the plugin. Every time this is called it should overwrite previous commands.
     */
    void registerCommands(List<CompiledCommand> compiledCommand);

    /**
     * Broadcast a message across the whole server
     *
     * @param message to broadcast
     */
    void broadcast(Component message);

    /**
     * Get default game mode set for the server
     *
     * @return game mode
     */
    GameMode getDefaultGameMode();
}
