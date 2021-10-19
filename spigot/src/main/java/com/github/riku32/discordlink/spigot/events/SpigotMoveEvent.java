package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.core.framework.eventbus.events.PlayerMoveEvent;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;

public class SpigotMoveEvent extends PlayerMoveEvent {
    private final org.bukkit.event.player.PlayerMoveEvent moveEvent;
    private final PlatformPlayer player;

    public SpigotMoveEvent(org.bukkit.event.player.PlayerMoveEvent moveEvent, PlatformPlayer player) {
        this.moveEvent = moveEvent;
        this.player = player;
    }

    @Override
    public PlatformPlayer getPlayer() {
        return player;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
        moveEvent.setCancelled(cancelled);
    }
}
