package com.github.riku32.discordlink.core.framework.eventbus.events;

/**
 * Fired when player joins the server
 */
public abstract class PlayerJoinEvent extends Event {
    /**
     * Get the join message to be broadcast
     *
     * @return join message
     */
    public abstract String getJoinMessage();

    /**
     * Set the join message to be broadcast
     */
    public abstract void setJoinMessage(String message);
}
