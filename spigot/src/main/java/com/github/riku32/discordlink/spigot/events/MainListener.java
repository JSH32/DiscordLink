package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.core.eventbus.EventBus;
import com.github.riku32.discordlink.spigot.events.chat.SpigotChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class MainListener implements Listener {
    private final EventBus eventBus;

    public MainListener(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent chatEvent) {
        eventBus.post(new SpigotChatEvent(chatEvent));
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent deathEvent) {
        eventBus.post(new SpigotDeathEvent(deathEvent));
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent joinEvent) {
        eventBus.post(new SpigotJoinEvent(joinEvent));
    }
}