package com.github.riku32.discordlink.spigot.old.events;

import com.github.riku32.discordlink.spigot.DiscordLinkSpigot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {
    private final DiscordLinkSpigot plugin;

    public PlayerMove(DiscordLinkSpigot plugin) {
        this.plugin = plugin;
    }

//    @EventHandler
//    private void onPlayerMove(PlayerMoveEvent e) {
//        if (plugin.getFrozenPlayers().contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
//    }
}