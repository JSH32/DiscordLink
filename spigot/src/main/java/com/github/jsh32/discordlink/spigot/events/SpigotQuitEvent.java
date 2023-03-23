package com.github.jsh32.discordlink.spigot.events;

import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.eventbus.events.PlayerQuitEvent;
import net.kyori.adventure.text.Component;

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
    public Component getQuitMessage() {
        return quitEvent.quitMessage();
    }

    @Override
    public void setQuitMessage(Component message) {
        quitEvent.quitMessage(message);
    }
}
