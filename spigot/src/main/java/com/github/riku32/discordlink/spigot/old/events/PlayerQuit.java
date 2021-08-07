package com.github.riku32.discordlink.spigot.old.events;

import com.github.riku32.discordlink.spigot.Util;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.spigot.Constants;
import com.github.riku32.discordlink.spigot.DiscordLinkSpigot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Optional;

public class PlayerQuit implements Listener {
    private final DiscordLinkSpigot plugin;

    public PlayerQuit(DiscordLinkSpigot plugin) {
        this.plugin = plugin;
    }

//    @EventHandler
//    private void onPlayerQuit(PlayerQuitEvent e) throws SQLException {
//        plugin.getBot().setPlayerCountStatus(plugin.getServer().getOnlinePlayers().size() - 1);
//        plugin.getFrozenPlayers().remove(e.getPlayer().getUniqueId());
//
//        if (plugin.getPluginConfig().isStatusEnabled()) e.setQuitMessage(null);
//
//        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
//        if (playerInfoOptional.isPresent() && playerInfoOptional.get().isVerified()) {
//            plugin.getBot().getGuild().retrieveMemberById((playerInfoOptional.get().getDiscordID())).queue(member -> {
//                if (plugin.getPluginConfig().isStatusEnabled()) {
//                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
//                            plugin.getPluginConfig().getStatusQuitLinked()
//                                    .replaceAll("%color%", Util.colorToChatString(
//                                            member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
//                                    .replaceAll("%username%", e.getPlayer().getName())
//                                    .replaceAll("%tag%", member.getUser().getAsTag())));
//                }
//
//                if (plugin.getPluginConfig().isChannelBroadcastQuit())
//                    plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
//                        .setColor(Constants.Colors.FAIL)
//                        .setAuthor(String.format("%s (%s) has left", e.getPlayer().getName(), member.getUser().getAsTag()),
//                                null, member.getUser().getAvatarUrl())
//                        .build())
//                        .queue();
//            });
//        } else if (!plugin.getPluginConfig().isLinkRequired()) {
//            if (plugin.getPluginConfig().isStatusEnabled()) {
//                e.setQuitMessage(ChatColor.translateAlternateColorCodes('&',
//                        plugin.getPluginConfig().getStatusQuitUnlinked()
//                                .replaceAll("%username%", e.getPlayer().getName())));
//            }
//
//            if (plugin.getPluginConfig().isChannelBroadcastQuit())
//                plugin.getBot().getChannel().sendMessage(new EmbedBuilder()
//                    .setColor(Constants.Colors.FAIL)
//                    .setAuthor(String.format("%s has left", e.getPlayer().getName()), null, Util.getHeadURL(e.getPlayer().getUniqueId()))
//                    .build())
//                    .queue();
//        }
//    }
}