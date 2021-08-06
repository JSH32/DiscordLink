package com.github.riku32.discordlink.spigot.events.chatevent;

import com.github.riku32.discordlink.core.eventbus.EventBus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {
    private final EventBus eventBus;

    public PlayerChat(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent e) {
        eventBus.post(new SpigotChatEvent(e));
    }
}
