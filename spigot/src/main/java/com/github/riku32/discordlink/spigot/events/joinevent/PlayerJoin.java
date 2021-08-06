package com.github.riku32.discordlink.spigot.events.joinevent;

import com.github.riku32.discordlink.core.eventbus.EventBus;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final EventBus eventBus;

    public PlayerJoin(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    private void onPlayerJoin(PlayerJoinEvent e) {
        eventBus.post(new SpigotPlayerJoin(e));
    }
}
