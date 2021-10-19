package com.github.riku32.discordlink.core.framework.eventbus.events;

import com.github.riku32.discordlink.core.framework.PlatformPlayer;

import java.util.Set;

/**
 * Event fired when a player sends a chat message
 */
public abstract class PlayerChatEvent extends Event {
    /**
     * Get the message that the user sent
     *
     * @return message
     */
    public abstract String getMessage();

    /**
     * Set the message that the user sent
     */
    public abstract void setMessage(String message);

    /**
     * Set the format that will be sent to users
     */
    public abstract void setFormat(String format);

    /**
     * Get all recipients that will receive this message
     *
     * @return recipients
     */
    public abstract Set<PlatformPlayer> getRecipients();
}
