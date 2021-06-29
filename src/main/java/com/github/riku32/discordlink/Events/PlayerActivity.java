package com.github.riku32.discordlink.Events;

import com.github.riku32.discordlink.DiscordLink;
import com.github.riku32.discordlink.PlayerInfo;
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

import java.awt.*;
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

        if ((boolean) Objects.requireNonNull(plugin.getConfig().get("status_messages.enabled")))
            e.setJoinMessage(null);

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
                                String.valueOf(plugin.getConfig().get("kick_messages.banned")))));
                    },
                    (not) -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (guild.getMemberById(user.getId()) == null) {
                                e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                        String.valueOf(plugin.getConfig().get("kick_messages.not_in_guild")).replaceAll("%tag%", user.getAsTag())));
                                return;
                            }

                            guild.retrieveMember(user).queue(member -> {
                                Color color = member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor();

                                if ((boolean) Objects.requireNonNull(plugin.getConfig().get("status_messages.enabled"))) {
                                    String joinMessage = ChatColor.translateAlternateColorCodes('&',
                                            String.valueOf(plugin.getConfig().get("status_messages.join"))
                                                    .replaceAll("%color%", colorToBungeeString(color))
                                                    .replaceAll("%username%", e.getPlayer().getDisplayName())
                                                    .replaceAll("%tag%", user.getAsTag()));
                                    Bukkit.broadcastMessage(joinMessage);
                                }
                            });
                        });
                    }
            );
        }, ignored -> {
            // Do not delete the user from database so they stay banned if they get ToS banned or deactivate account
            e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                    String.valueOf(plugin.getConfig().get("kick_messages.tos"))));
        });
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) throws SQLException {
        setPlayerCount(plugin.getServer().getOnlinePlayers().size() - 1);
        plugin.getFrozenPlayers().remove(e.getPlayer().getUniqueId());

        if ((boolean) Objects.requireNonNull(plugin.getConfig().get("status_messages.enabled"))) {
            e.setQuitMessage(null); // Disable default quit message since we need to send one in async task

            Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
            if (playerInfoOptional.isPresent() && playerInfoOptional.get().isVerified()) {
                plugin.getBot().getGuild().retrieveMemberById((playerInfoOptional.get().getDiscordID())).queue(member -> {
                    Color color = member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor();
                    String quitMessage = ChatColor.translateAlternateColorCodes('&',
                            String.valueOf(plugin.getConfig().get("status_messages.quit"))
                                    .replaceAll("%color%", colorToBungeeString(color))
                                    .replaceAll("%username%", e.getPlayer().getDisplayName())
                                    .replaceAll("%tag%", member.getUser().getAsTag()));
                    Bukkit.broadcastMessage(quitMessage);
                });
            }
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) throws SQLException {
        if (!(boolean) Objects.requireNonNull(plugin.getConfig().get("status_messages.enabled"))) return;

        final String causeWithoutName;
        if (e.getDeathMessage() == null)
            causeWithoutName = "died";
        else
            causeWithoutName = e.getDeathMessage().substring(e.getDeathMessage().indexOf(" ") + 1);

        // Send death through broadcast instead due to async
        e.setDeathMessage(null);

        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getEntity().getUniqueId());
        if (playerInfoOptional.isPresent() && playerInfoOptional.get().isVerified()) {
            plugin.getBot().getGuild().retrieveMemberById((playerInfoOptional.get().getDiscordID())).queue(member -> {
                Color color = member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor();
                String deathMessage = ChatColor.translateAlternateColorCodes('&',
                        String.valueOf(plugin.getConfig().get("status_messages.death"))
                                .replaceAll("%color%", colorToBungeeString(color))
                                .replaceAll("%username%", e.getEntity().getDisplayName())
                                .replaceAll("%tag%", member.getUser().getAsTag()))
                                .replaceAll("%cause%", causeWithoutName);
                Bukkit.broadcastMessage(deathMessage);
            });
        }
    }

    private String colorToBungeeString(Color color) {
        String hexColor = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        String newString = ChatColor.COLOR_CHAR + "x";

        for (int i = 0; i < hexColor.length(); i++)
            newString = newString.concat(String.valueOf(ChatColor.COLOR_CHAR) + hexColor.charAt(i));

        return newString;
    }

    public void setPlayerCount(int playerCount) {
        plugin.getBot().getJda().getPresence()
                .setActivity(Activity.watching(String.format("%d people play minecraft", playerCount)));
    }
}
