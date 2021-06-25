package com.github.riku32.discordlink.Events;

import com.github.riku32.discordlink.DiscordLink;
import com.github.riku32.discordlink.PlayerInfo;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Objects;
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

            plugin.getFrozenPlayers().add(e.getPlayer().getUniqueId());
            e.setJoinMessage(null);
        } else if (!playerInfoOptional.get().isVerified()) {
            User user;
            try {
                user = plugin.getJda().retrieveUserById((playerInfoOptional.get().getDiscordID())).complete();
            } catch (ErrorResponseException error) {
                // Account does not exist, since verification is not complete just delete all the data
                plugin.getDatabase().deletePlayer(e.getPlayer().getUniqueId());
                plugin.getDatabase().deleteVerificationMessage(e.getPlayer().getUniqueId());
                return;
            }

            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    String.format("&7Please verify your account. Respond to the discord DM to &e%s&7 from &e%s",
                            user.getAsTag(), plugin.getJda().getSelfUser().getAsTag())));
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&7If this is the wrong account do &e/unlink &7or press the red button on the discord DM"));

            plugin.getFrozenPlayers().add(e.getPlayer().getUniqueId());
            e.setJoinMessage(null);
        } else {
            Guild guild = Objects.requireNonNull(plugin.getJda().getGuildById(plugin.getGuildID()));
            User user;
            try {
                user = plugin.getJda().retrieveUserById((playerInfoOptional.get().getDiscordID())).complete();
            } catch (ErrorResponseException error) {
                // Do not delete the user from database so they stay banned if they get ToS banned or deactivate account
                e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                        String.valueOf(plugin.getConfig().get("kick_messages.tos"))));
                return;
            }

            guild.retrieveBan(user).queue(
                    (banned) -> {
                        Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                String.valueOf(plugin.getConfig().get("kick_messages.banned")))));
                    },
                    (not) -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (guild.getMemberById(user.getId()) == null) {
                                e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                        String.valueOf(plugin.getConfig().get("kick_messages.not_in_guild")).replaceAll("%tag%", user.getAsTag())));
                            }
                        });
                    }
            );
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) throws SQLException {
        setPlayerCount(plugin.getServer().getOnlinePlayers().size() - 1);

        plugin.getFrozenPlayers().remove(e.getPlayer().getUniqueId());

        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
        if (!playerInfoOptional.isPresent() || !playerInfoOptional.get().isVerified())
            e.setQuitMessage(null);
    }

    public void setPlayerCount(int playerCount) {
        plugin.getJda().getPresence()
                .setActivity(Activity.watching(String.format("%d people play minecraft", playerCount)));
    }
}
