package com.github.riku32.discordlink.Events;

import com.github.riku32.discordlink.DiscordLink;
import com.github.riku32.discordlink.PlayerInfo;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Optional;

public class PlayerActivity implements Listener {
    private final DiscordLink plugin;

    public PlayerActivity(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) throws SQLException {
        setPlayerCount(plugin.getServer().getOnlinePlayers().size());

        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
        if (!playerInfoOptional.isPresent()) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&7You are not registered, type &e/link <your discord tag> &7to play on this server"));
            e.setJoinMessage(null);
        } else if (!playerInfoOptional.get().isVerified()) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    String.format("&7Please verify your account. Respond to the discord DM to &e%s&7 from &e%s",
                            plugin.getJda().retrieveUserById(playerInfoOptional.get().getDiscordID()).complete().getAsTag(),
                            plugin.getJda().getSelfUser().getAsTag())));
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&7If this is the wrong account do &e/unlink &7or press the red button on the discord DM"));
            e.setJoinMessage(null);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) throws SQLException {
        setPlayerCount(plugin.getServer().getOnlinePlayers().size() - 1);

        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
        if (!playerInfoOptional.isPresent() || !playerInfoOptional.get().isVerified())
            e.setQuitMessage(null);
    }

    private void setPlayerCount(int playerCount) {
        plugin.getJda().getPresence()
                .setActivity(Activity.watching(String.format("%d people play minecraft", playerCount)));
    }
}
