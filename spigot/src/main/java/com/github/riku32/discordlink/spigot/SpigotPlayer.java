package com.github.riku32.discordlink.spigot;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SpigotPlayer implements PlatformPlayer {
    public final Player player;

    public SpigotPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUuid() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public Object getPlatformPlayer() {
        return player;
    }
}
