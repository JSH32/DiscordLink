package com.github.riku32.discordlink.core.events;

import com.github.riku32.discordlink.core.eventbus.annotation.EventHandler;
import com.github.riku32.discordlink.core.eventbus.events.PlayerJoinEvent;
import net.dv8tion.jda.api.entities.TextChannel;

public class JoinEvent {
    private TextChannel channel;

    public JoinEvent(TextChannel channel) {
        this.channel = channel;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        channel.sendMessage(event.getPlayer().getName() + " joined the server").queue();
    }
}
