package com.github.riku32.discordlink.core.framework.eventbus.events;

import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import net.kyori.adventure.text.Component;

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
    public abstract Component getMessage();

    /**
     * Set the message that the user sent
     */
    public abstract void setMessage(Component message);

    /**
     * Get all recipients that will receive this message
     *
     * @return recipients
     */
    public abstract Set<PlatformPlayer> getRecipients();
}
