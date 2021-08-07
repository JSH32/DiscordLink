package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.core.platform.events.PlayerJoinEvent;
import com.github.riku32.discordlink.spigot.SpigotPlayer;

public class SpigotJoinEvent extends PlayerJoinEvent {
    private final org.bukkit.event.player.PlayerJoinEvent joinEvent;
    private final PlatformPlayer player;

    public SpigotJoinEvent(org.bukkit.event.player.PlayerJoinEvent joinEvent) {
        this.joinEvent = joinEvent;
        this.player = new SpigotPlayer(joinEvent.getPlayer());
    }

    @Override
    public PlatformPlayer getPlayer() {
        return player;
    }

    @Override
    public String getJoinMessage() {
        return joinEvent.getJoinMessage();
    }

    @Override
    public void setJoinMessage(String message) {
        joinEvent.setJoinMessage(message);
    }
}
