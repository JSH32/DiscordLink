package com.github.riku32.discordlink.core.framework.eventbus.events;

/**
 * Fired when player dies
 */
public abstract class PlayerDeathEvent extends Event {
    /**
     * Get the death message to be broadcast
     *
     * @return death message
     */
    public abstract String getDeathMessage();

    /**
     * Set the death message to be broadcast
     */
    public abstract void setDeathMessage(String message);
}
