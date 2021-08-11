package com.github.riku32.discordlink.core.events;

import com.github.riku32.discordlink.core.eventbus.annotation.EventHandler;
import com.github.riku32.discordlink.core.eventbus.events.PlayerJoinEvent;
import net.dv8tion.jda.api.entities.TextChannel;

public class JoinEvent {
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome");
    }
}
