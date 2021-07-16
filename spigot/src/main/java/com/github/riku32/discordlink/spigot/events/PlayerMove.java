package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.spigot.DiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {
    private final DiscordLink plugin;

    public PlayerMove(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e) {
        if (plugin.getFrozenPlayers().contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }
}