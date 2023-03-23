package com.github.jsh32.discordlink.spigot.events;

import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.eventbus.events.PlayerJoinEvent;
import net.kyori.adventure.text.Component;

public class SpigotJoinEvent extends PlayerJoinEvent {
    private final org.bukkit.event.player.PlayerJoinEvent joinEvent;
    private final PlatformPlayer player;

    public SpigotJoinEvent(org.bukkit.event.player.PlayerJoinEvent joinEvent, PlatformPlayer player) {
        this.joinEvent = joinEvent;
        this.player = player;
    }

    @Override
    public PlatformPlayer getPlayer() {
        return player;
    }

    @Override
    public Component getJoinMessage() {
        return joinEvent.joinMessage();
    }

    @Override
    public void setJoinMessage(Component message) {
        joinEvent.joinMessage(message);
    }
}
