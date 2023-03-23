package com.github.jsh32.discordlink.spigot;

import com.github.jsh32.discordlink.core.framework.GameMode;
import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
    public void sendMessage(Component message) {
        player.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String node) {
        return player.isOp() || player.hasPermission(node);
    }

    @Override
    public Object getPlatformPlayer() {
        return player;
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        Bukkit.getScheduler().runTask(DiscordLinkSpigot.INSTANCE, () -> {
            switch (gameMode) {
                case CREATIVE:
                    player.setGameMode(org.bukkit.GameMode.CREATIVE);
                    break;
                case SPECTATOR:
                    player.setGameMode(org.bukkit.GameMode.SPECTATOR);
                    break;
                case SURVIVAL:
                    player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    break;
                case ADVENTURE:
                    player.setGameMode(org.bukkit.GameMode.ADVENTURE);
                    break;
            }
        });
    }

    @Override
    public void kickPlayer(Component message) {
        player.kick(message);
    }
}
