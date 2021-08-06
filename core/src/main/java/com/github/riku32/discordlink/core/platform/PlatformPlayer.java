package com.github.riku32.discordlink.core.platform;

import java.util.UUID;

/**
 * Platform agnostic player implementation
 */
public interface PlatformPlayer {
    /**
     * Get the UUID of the player
     *
     * @return UUID
     */
    UUID getUuid();

    /**
     * Get the player name
     *
     * @return player name
     */
    String getName();

    /**
     * Send message to player
     *
     * @param message message to send to player
     */
    void sendMessage(String message);

    /**
     * Get the player object behind the implementation
     *
     * @return player
     */
    Object getPlatformPlayer();
}
