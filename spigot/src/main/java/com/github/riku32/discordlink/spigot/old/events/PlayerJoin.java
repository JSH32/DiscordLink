package com.github.riku32.discordlink.spigot.old.events;

import com.github.riku32.discordlink.spigot.Util;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.spigot.Constants;
import com.github.riku32.discordlink.spigot.DiscordLinkSpigot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.Optional;

public class PlayerJoin implements Listener {
    private final DiscordLinkSpigot plugin;

    public PlayerJoin(DiscordLinkSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) throws SQLException {
        plugin.getBot().setPlayerCountStatus(plugin.getServer().getOnlinePlayers().size());

        if (plugin.getPluginConfig().isStatusEnabled()) e.setJoinMessage(null);

        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
        if (playerInfoOptional.isEmpty()) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&7You are not registered, type &e/link <your discord tag> " +
                            (plugin.getPluginConfig().isLinkRequired() ? "&7to play on this server" : "")));

            if (plugin.getPluginConfig().isLinkRequired()) {
                e.getPlayer().setGameMode(GameMode.SPECTATOR);
                plugin.getFrozenPlayers().add(e.getPlayer().getUniqueId());
            }
        } else if (!playerInfoOptional.get().isVerified()) {
            plugin.getBot().getJda().retrieveUserById((playerInfoOptional.get().getDiscordID())).queue(user -> {
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("&7Please verify your account link. Respond to the discord DM to &e%s&7 from &e%s",
                                user.getAsTag(), plugin.getBot().getJda().getSelfUser().getAsTag())));
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&7If this is the wrong account do &e/cancel &7or press the cancel button on the discord DM"));

                if (plugin.getPluginConfig().isLinkRequired()) {
                    plugin.getFrozenPlayers().add(e.getPlayer().getUniqueId());
                    Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().setGameMode(GameMode.SPECTATOR));
                }
            }, ignored -> {
                // User is invalid/left before verification, just remove the data that was leftover
                try {
                    plugin.getDatabase().deletePlayer(e.getPlayer().getUniqueId());
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            });
        }

        // If player is not linked
        if (playerInfoOptional.isEmpty() || !playerInfoOptional.get().isVerified()) {
            if (plugin.getPluginConfig().isLinkRequired()) return;

            if (plugin.getPluginConfig().isChannelBroadcastJoin())
                plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Constants.Colors.SUCCESS)
                        .setAuthor(e.getPlayer().getName() + " has joined", null, Util.getHeadURL(e.getPlayer().getUniqueId()))
                        .build())
                        .queue();

            if (plugin.getPluginConfig().isStatusEnabled()) {
                String joinMessage = ChatColor.translateAlternateColorCodes('&', plugin.getPluginConfig().getStatusJoinUnlinked()
                        .replaceAll("%username%", e.getPlayer().getName()));
                e.setJoinMessage(joinMessage);
            }

            return;
        }

        // If player is linked
        plugin.getBot().getJda().retrieveUserById((playerInfoOptional.get().getDiscordID())).queue(user -> {
            Guild guild = plugin.getBot().getGuild();
            guild.retrieveBan(user).queue(
                    (banned) -> Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                            String.valueOf(plugin.getPluginConfig().getKickBanned())))),
                    (not) -> guild.retrieveMember(user).queue(member -> {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    if (plugin.getPluginConfig().isStatusEnabled()) {
                                        Bukkit.broadcastMessage(
                                                ChatColor.translateAlternateColorCodes('&', plugin.getPluginConfig().getStatusJoinLinked()
                                                        .replaceAll("%color%", Util.colorToChatString(
                                                                member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                                                        .replaceAll("%username%", e.getPlayer().getName())
                                                        .replaceAll("%tag%", user.getAsTag())));
                                    }

                                    e.getPlayer().setGameMode(plugin.getServer().getDefaultGameMode());
                                });

                                if (plugin.getPluginConfig().isChannelBroadcastJoin())
                                    plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                                                .setColor(Constants.Colors.SUCCESS)
                                                .setAuthor(String.format("%s (%s) has joined", e.getPlayer().getName(), user.getAsTag()),
                                                        null, user.getAvatarUrl())
                                                .build())
                                                .queue();
                            }, ignored -> {
                                // User left server but unlink is allowed so just delete their account
                                if (plugin.getPluginConfig().isAllowUnlink()) {
                                    try {
                                        plugin.getDatabase().deletePlayer(e.getPlayer().getUniqueId());
                                    } catch (SQLException exception) {
                                        exception.printStackTrace();
                                    }

                                    e.getPlayer().sendMessage(ChatColor.RED + "You have left the discord server, account automatically unlinked from discord");

                                    if (plugin.getPluginConfig().isLinkRequired()) {
                                        plugin.getFrozenPlayers().add(e.getPlayer().getUniqueId());

                                        Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().setGameMode(GameMode.SPECTATOR));
                                        e.getPlayer().sendMessage(ChatColor.RED + "Please relink your account with " + ChatColor.YELLOW + "/link <your discord tag>");
                                    }

                                    return;
                                }

                                e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getPluginConfig().getKickNotInGuild().replaceAll("%tag%", user.getAsTag())));
                            }
                    ));
        }, ignored -> {
            // Do not delete the user from database so they stay banned if they get ToS banned or deactivate account
            e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                    plugin.getPluginConfig().getKickToS()));
        });
    }
}