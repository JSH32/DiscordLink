package com.github.jsh32.discordlink.spigot.events;

import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.eventbus.events.PlayerDeathEvent;
import net.kyori.adventure.text.Component;

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
    public Component getDeathMessage() {
        return deathEvent.deathMessage();
    }

    @Override
    public void setDeathMessage(Component message) {
        deathEvent.deathMessage(message);
    }
}
