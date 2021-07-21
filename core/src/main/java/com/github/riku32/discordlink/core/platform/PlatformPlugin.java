package com.github.riku32.discordlink.core.platform;

import java.util.UUID;

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
     * Log a message to the console
     *
     * @param message to log
     */
    void log(String message);
}
