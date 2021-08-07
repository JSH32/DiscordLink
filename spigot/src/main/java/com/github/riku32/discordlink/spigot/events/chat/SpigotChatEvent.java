package com.github.riku32.discordlink.spigot.events.chat;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.core.platform.events.PlayerChatEvent;
import com.github.riku32.discordlink.spigot.SpigotPlayer;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class SpigotChatEvent extends PlayerChatEvent {
    private final AsyncPlayerChatEvent chatEvent;
    private final PlatformPlayer player;

    public SpigotChatEvent(AsyncPlayerChatEvent chatEvent) {
        this.chatEvent = chatEvent;
        this.player = new SpigotPlayer(chatEvent.getPlayer());
    }

    @Override
    public PlatformPlayer getPlayer() {
        return player;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
        chatEvent.setCancelled(true);
    }

    @Override
    public String getMessage() {
        return chatEvent.getMessage();
    }

    @Override
    public void setMessage(String message) {
        chatEvent.setMessage(message);
    }

    @Override
    public void setFormat(String format) {
        chatEvent.setFormat(format);
    }

    @Override
    public Set<PlatformPlayer> getRecipients() {
        return new RecipientSet<>(chatEvent.getRecipients());
    }
}
