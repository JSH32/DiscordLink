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
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&7You are not registered, type &e/link <your discord tag> &7to play on this server"));

            plugin.getFrozenPlayers().add(e.getPlayer().getUniqueId());
            return;
        } else if (!playerInfoOptional.get().isVerified()) {
            plugin.getBot().getJda().retrieveUserById((playerInfoOptional.get().getDiscordID())).queue(user -> {
                e.getPlayer().setGameMode(GameMode.SPECTATOR);
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("&7Please verify your account. Respond to the discord DM to &e%s&7 from &e%s",
                                user.getAsTag(), plugin.getBot().getJda().getSelfUser().getAsTag())));
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&7If this is the wrong account do &e/cancel &7or press the cancel button on the discord DM"));

                plugin.getFrozenPlayers().add(e.getPlayer().getUniqueId());
            }, ignored -> {
                try {
                    plugin.getDatabase().deletePlayer(e.getPlayer().getUniqueId());
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            });

            return;
        }

        plugin.getBot().getJda().retrieveUserById((playerInfoOptional.get().getDiscordID())).queue(user -> {
            Guild guild = plugin.getBot().getGuild();
            guild.retrieveBan(user).queue(
                    (banned) -> {
                        Bukkit.getScheduler().runTask(plugin, () -> e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                String.valueOf(plugin.getPluginConfig().getKickBanned()))));
                    },
                    (not) -> {
                        guild.retrieveMember(user).queue(member -> {
                            if (plugin.getPluginConfig().isStatusEnabled()) {
                                String joinMessage = ChatColor.translateAlternateColorCodes('&', plugin.getPluginConfig().getStatusJoin()
                                                .replaceAll("%color%", Util.colorToChatString(
                                                        member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                                                .replaceAll("%username%", e.getPlayer().getDisplayName())
                                                .replaceAll("%tag%", user.getAsTag()));
                                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(joinMessage));
                            }

                            e.getPlayer().setGameMode(plugin.getServer().getDefaultGameMode());
                            if (plugin.getBot().getChannel() != null)
                                plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                                        .setColor(Constants.Colors.SUCCESS)
                                        .setAuthor(String.format("%s (%s) has joined", user.getName(), Bukkit.getOfflinePlayer(playerInfoOptional.get().getUuid()).getName()),
                                                null, user.getAvatarUrl())
                                        .build())
                                        .queue();
                        }, ignored -> {
                            e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getPluginConfig().getKickNotInGuild().replaceAll("%tag%", user.getAsTag())));
                        });
                    }
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
                    String quitMessage = ChatColor.translateAlternateColorCodes('&',
                            plugin.getPluginConfig().getStatusQuit()
                                    .replaceAll("%color%", Util.colorToChatString(
                                            member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                                    .replaceAll("%username%", e.getPlayer().getDisplayName())
                                    .replaceAll("%tag%", member.getUser().getAsTag()));
                    Bukkit.broadcastMessage(quitMessage);
                }

                if (plugin.getBot().getChannel() != null)
                    plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                            .setColor(Constants.Colors.FAIL)
                            .setAuthor(String.format("%s (%s) has left", member.getUser().getName(), Bukkit.getOfflinePlayer(playerInfoOptional.get().getUuid()).getName()),
                                    null, member.getUser().getAvatarUrl())
                            .build())
                            .queue();
            });
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
                            plugin.getPluginConfig().getStatusDeath()
                                    .replaceAll("%color%", Util.colorToChatString(
                                            member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                                    .replaceAll("%username%", e.getEntity().getDisplayName())
                                    .replaceAll("%tag%", member.getUser().getAsTag()))
                            .replaceAll("%cause%", causeWithoutName));
                }

                if (plugin.getBot().getChannel() != null)
                    plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                            .setColor(Constants.Colors.FAIL)
                            .setAuthor(String.format("%s (%s) %s", member.getUser().getName(),
                                    Bukkit.getOfflinePlayer(playerInfoOptional.get().getUuid()).getName(), causeWithoutName),
                                    null, member.getUser().getAvatarUrl())
                            .build())
                            .queue();
            });
        }
    }

    public void setPlayerCount(int playerCount) {
        plugin.getBot().getJda().getPresence()
                .setActivity(Activity.watching(String.format("%d people play minecraft", playerCount)));
    }
}
