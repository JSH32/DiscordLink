package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.core.framework.eventbus.events.PlayerQuitEvent;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;

public class SpigotQuitEvent extends PlayerQuitEvent {
    private final org.bukkit.event.player.PlayerQuitEvent quitEvent;
    private final PlatformPlayer player;

    public SpigotQuitEvent(org.bukkit.event.player.PlayerQuitEvent quitEvent, PlatformPlayer player) {
        this.quitEvent = quitEvent;
        this.player = player;
    }

    @Override
    public PlatformPlayer getPlayer() {
        return player;
    }

    @Override
    public String getQuitMessage() {
        return quitEvent.getQuitMessage();
    }

    @Override
    public void setQuitMessage(String message) {
        quitEvent.setQuitMessage(message);
    }
}
