package com.github.riku32.discordlink.core.platform.events;

/**
 * Fired when player leaves the server
 */
public abstract class PlayerQuitEvent extends Event {
    /**
     * Get the quit message to be broadcast
     *
     * @return quit message
     */
    public abstract String getQuitMessage();

    /**
     * Set the quit message to be broadcast
     */
    public abstract void setQuitMessage(String message);
}
