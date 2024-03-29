package com.github.jsh32.discordlink.spigot;

import com.github.jsh32.discordlink.core.framework.PlatformOfflinePlayer;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class SpigotOfflinePlayer implements PlatformOfflinePlayer {
    private final OfflinePlayer offlinePlayer;

    public SpigotOfflinePlayer(OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
    }

    @Override
    public UUID getUuid() { return offlinePlayer.getUniqueId(); }

    @Override
    public String getName() { return offlinePlayer.getName(); }

    @Override
    public boolean isOnline() {return offlinePlayer.isOnline(); }
}
