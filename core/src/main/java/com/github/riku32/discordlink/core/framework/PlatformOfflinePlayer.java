package com.github.riku32.discordlink.core.framework;

import java.util.UUID;

/**
 * Represents a player who is not currently online.
 */
public interface PlatformOfflinePlayer {
    UUID getUuid();
    String getName();
    boolean isOnline();
}
