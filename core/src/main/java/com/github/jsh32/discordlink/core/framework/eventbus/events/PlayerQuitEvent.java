package com.github.jsh32.discordlink.core.framework.eventbus.events;

import net.kyori.adventure.text.Component;

/**
 * Fired when player leaves the server
 */
public abstract class PlayerQuitEvent extends Event {
    /**
     * Get the quit message to be broadcast
     *
     * @return quit message
     */
    public abstract Component getQuitMessage();

    /**
     * Set the quit message to be broadcast
     */
    public abstract void setQuitMessage(Component message);
}
