package com.github.riku32.discordlink.Events;

import com.github.riku32.discordlink.DiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {
    private final DiscordLink plugin;

    public PlayerChat(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent e) {
        if (plugin.getFrozenPlayers().contains(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }
}