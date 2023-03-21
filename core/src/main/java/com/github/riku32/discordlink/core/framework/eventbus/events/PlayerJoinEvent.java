package com.github.riku32.discordlink.core.framework.eventbus.events;

import net.kyori.adventure.text.Component;

/**
 * Fired when player joins the server
 */
public abstract class PlayerJoinEvent extends Event {
    /**
     * Get the join message to be broadcast
     *
     * @return join message
     */
    public abstract Component getJoinMessage();

    /**
     * Set the join message to be broadcast
     */
    public abstract void setJoinMessage(Component message);
}
