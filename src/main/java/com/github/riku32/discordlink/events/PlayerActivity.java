package com.github.riku32.discordlink.events;

import com.github.riku32.discordlink.Constants;
import com.github.riku32.discordlink.DiscordLink;
import com.github.riku32.discordlink.PlayerInfo;
import com.github.riku32.discordlink.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.md_5.bungee.api.ChatColor;

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

        if (plugin.getPluginConfig().isStatusEnabled()) e.setJoinMessage(null);

        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
        if (!playerInfoOptional.isPresent()) {
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
                    e.getPlayer().setGameMode(GameMode.SPECTATOR);
                    plugin.getFrozenPlayers().add(e.getPlayer().getUniqueId());
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
        if (!playerInfoOptional.isPresent() || !playerInfoOptional.get().isVerified()) {
            if (plugin.getPluginConfig().isLinkRequired()) return;

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
                        if (plugin.getPluginConfig().isStatusEnabled()) {
                            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(
                                    ChatColor.translateAlternateColorCodes('&', plugin.getPluginConfig().getStatusJoinLinked()
                                        .replaceAll("%color%", Util.colorToChatString(
                                                member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                                        .replaceAll("%username%", e.getPlayer().getName())
                                        .replaceAll("%tag%", user.getAsTag()))));
                        }

                        e.getPlayer().setGameMode(plugin.getServer().getDefaultGameMode());
                        plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                                .setColor(Constants.Colors.SUCCESS)
                                .setAuthor(String.format("%s (%s) has joined", e.getPlayer().getName(), user.getName()),
                                        null, user.getAvatarUrl())
                                .build())
                                .queue();
                    }, ignored -> e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                            plugin.getPluginConfig().getKickNotInGuild().replaceAll("%tag%", user.getAsTag()))))
            );
        }, ignored -> {
            // Do not delete the user from database so they stay banned if they get ToS banned or deactivate account
            e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                    plugin.getPluginConfig().getKickToS()));
        });
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) throws SQLException {
        setPlayerCount(plugin.getServer().getOnlinePlayers().size() - 1);
        plugin.getFrozenPlayers().remove(e.getPlayer().getUniqueId());

        if (plugin.getPluginConfig().isStatusEnabled()) e.setQuitMessage(null);

        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
        if (playerInfoOptional.isPresent() && playerInfoOptional.get().isVerified()) {
            plugin.getBot().getGuild().retrieveMemberById((playerInfoOptional.get().getDiscordID())).queue(member -> {
                if (plugin.getPluginConfig().isStatusEnabled()) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getPluginConfig().getStatusQuitLinked()
                                    .replaceAll("%color%", Util.colorToChatString(
                                            member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                                    .replaceAll("%username%", e.getPlayer().getName())
                                    .replaceAll("%tag%", member.getUser().getAsTag())));
                }

                plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Constants.Colors.FAIL)
                        .setAuthor(String.format("%s (%s) has left", e.getPlayer().getName(), member.getUser().getName()),
                                null, member.getUser().getAvatarUrl())
                        .build())
                        .queue();
            });
        } else if (!plugin.getPluginConfig().isLinkRequired()) {
            if (plugin.getPluginConfig().isStatusEnabled()) {
                e.setQuitMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPluginConfig().getStatusQuitUnlinked()
                                .replaceAll("%username%", e.getPlayer().getName())));
            }

            plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                    .setColor(Constants.Colors.FAIL)
                    .setAuthor(String.format("%s has left", e.getPlayer().getName()), null, Util.getHeadURL(e.getPlayer().getUniqueId()))
                    .build())
                    .queue();
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) throws SQLException {
        final String causeWithoutName;
        if (e.getDeathMessage() == null)
            causeWithoutName = "died";
        else
            causeWithoutName = e.getDeathMessage().substring(e.getDeathMessage().indexOf(" ") + 1).replaceAll("\n", "");

        if (plugin.getPluginConfig().isStatusEnabled()) e.setDeathMessage(null);

        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getEntity().getUniqueId());
        if (playerInfoOptional.isPresent() && playerInfoOptional.get().isVerified()) {
            plugin.getBot().getGuild().retrieveMemberById((playerInfoOptional.get().getDiscordID())).queue(member -> {
                // Send custom death message if status is enabled, else handle normally
                if (plugin.getPluginConfig().isStatusEnabled()) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getPluginConfig().getStatusDeathLinked()
                                    .replaceAll("%color%", Util.colorToChatString(
                                            member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                                    .replaceAll("%username%", e.getEntity().getDisplayName())
                                    .replaceAll("%tag%", member.getUser().getAsTag())
                                    .replaceAll("%cause%", causeWithoutName)));
                }

                plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Constants.Colors.FAIL)
                        .setAuthor(String.format("%s (%s) %s", e.getEntity().getName(), member.getUser().getName(), causeWithoutName),
                                null, member.getUser().getAvatarUrl())
                        .build())
                        .queue();
            });
        } else if (!plugin.getPluginConfig().isLinkRequired()) {
            if (plugin.getPluginConfig().isStatusEnabled()) {
                e.setDeathMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPluginConfig().getStatusDeathUnlinked()
                                .replaceAll("%username%", e.getEntity().getName())
                                .replaceAll("%cause%", causeWithoutName)));
            }

            plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                    .setColor(Constants.Colors.FAIL)
                    .setAuthor(String.format("%s %s", e.getEntity().getName(), causeWithoutName), null, Util.getHeadURL(e.getEntity().getUniqueId()))
                    .build())
                    .queue();
        }
    }

    public void setPlayerCount(int playerCount) {
        plugin.getBot().getJda().getPresence()
                .setActivity(Activity.watching(String.format("%d people play minecraft", playerCount)));
    }
}
