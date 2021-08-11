package com.github.riku32.discordlink.core.eventbus.events;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;

/**
 * Base event interface
 */
public abstract class Event {
    private boolean isCancelled = false;

    /**
     * Get player who sent the message
     *
     * @return player
     */
    public abstract PlatformPlayer getPlayer();

    /**
     * Cancel the event from firing, no other listeners will be fired after this is called
     */
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    /**
     * Check if the event was cancelled
     *
     * @return cancelled
     */
    public boolean isCancelled() {
        return isCancelled;
    }
}
