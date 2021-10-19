package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.framework.eventbus.events.PlayerDeathEvent;

public class SpigotDeathEvent extends PlayerDeathEvent {
    private final org.bukkit.event.entity.PlayerDeathEvent deathEvent;
    private final PlatformPlayer player;

    public SpigotDeathEvent(org.bukkit.event.entity.PlayerDeathEvent deathEvent, PlatformPlayer player) {
        this.deathEvent = deathEvent;
        this.player = player;
    }

    @Override
    public PlatformPlayer getPlayer() {
        return player;
    }

    @Override
    public String getDeathMessage() {
        return deathEvent.getDeathMessage();
    }

    @Override
    public void setDeathMessage(String message) {
        deathEvent.setDeathMessage(message);
    }
}
