package com.github.riku32.discordlink.core.framework;

import com.github.riku32.discordlink.core.framework.eventbus.EventBus;
import com.github.riku32.discordlink.core.framework.command.CompiledCommand;

import java.io.File;
import java.nio.ByteBuffer;
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
     * Register a command into the platforms command registry
     */
    void registerCommand(CompiledCommand compiledCommand);

    /**
     * Broadcast a message across the whole server
     *
     * @param message to broadcast
     */
    void broadcast(String message);

    /**
     * Get default game mode set for the server
     *
     * @return game mode
     */
    GameMode getDefaultGameMode();

    /**
     * Check if a user is online based on their UUID
     */
    boolean isOnline(UUID uuid);

    /**
     * Check if a user is online based on their name
     */
    boolean isOnline(String name);
}
