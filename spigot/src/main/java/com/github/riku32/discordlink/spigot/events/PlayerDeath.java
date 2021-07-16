package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.spigot.Util;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.spigot.Constants;
import com.github.riku32.discordlink.spigot.DiscordLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.sql.SQLException;
import java.util.Optional;

public class PlayerDeath implements Listener {
    private final DiscordLink plugin;

    public PlayerDeath(DiscordLink plugin) {
        this.plugin = plugin;
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

                if (plugin.getPluginConfig().isChannelBroadcastDeath())
                    plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                            .setColor(Constants.Colors.FAIL)
                            .setAuthor(String.format("%s (%s) %s", e.getEntity().getName(), member.getUser().getAsTag(), causeWithoutName),
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

            if (plugin.getPluginConfig().isChannelBroadcastDeath())
                plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Constants.Colors.FAIL)
                        .setAuthor(String.format("%s %s", e.getEntity().getName(), causeWithoutName), null, Util.getHeadURL(e.getEntity().getUniqueId()))
                        .build())
                        .queue();
        }
    }
}