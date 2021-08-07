package com.github.riku32.discordlink.core.events;

import com.github.riku32.discordlink.core.eventbus.annotation.EventHandler;
import com.github.riku32.discordlink.core.platform.events.PlayerJoinEvent;

public class JoinEvent {
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome");
    }
}
