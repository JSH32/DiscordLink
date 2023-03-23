package com.github.jsh32.discordlink.spigot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Preserve object equality and prevent creation of multiple identical {@link SpigotPlayer} objects
 * <p>
 * This must be registered as a listener on spigot
 */
public class PlayerRegistry implements Listener {
    private final Map<Player, SpigotPlayer> convertedPlayerMap = new HashMap<>();

    public SpigotPlayer getPlayer(@NotNull Player player) {
        SpigotPlayer spigotPlayer = convertedPlayerMap.get(player);
        if (spigotPlayer == null) {
            spigotPlayer = new SpigotPlayer(player);
            convertedPlayerMap.put(player, spigotPlayer);
        }

        return spigotPlayer;
    }

    // Run after every other event to clean up the player map
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent event) {
        convertedPlayerMap.remove(event.getPlayer());
    }
}
