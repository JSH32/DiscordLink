package com.github.riku32.discordlink.core.platform;

import com.github.riku32.discordlink.core.eventbus.EventBus;

import java.io.File;
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
}
