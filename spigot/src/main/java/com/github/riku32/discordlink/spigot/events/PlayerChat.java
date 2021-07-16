package com.github.riku32.discordlink.spigot.events;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;

import com.github.riku32.discordlink.spigot.Util;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.spigot.DiscordLink;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.Optional;

public class PlayerChat implements Listener {
    private final DiscordLink plugin;

    private final WebhookClient messageRelay;

    /**
     * Player chat event handler.
     *
     * @param plugin DiscordLink plugin instance
     * @param messageRelay webhook to send in-game messages to, if {@code null} messages will not be relayed
     */
    public PlayerChat(DiscordLink plugin, WebhookClient messageRelay) {
        this.plugin = plugin;
        this.messageRelay = messageRelay;
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent e) throws SQLException {
        // Cancel if player is frozen for whatever reason
        if (plugin.getFrozenPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            if (plugin.getPluginConfig().isLinkRequired())
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&cYou need to link a Discord account to play on this server. Please type &e/link <your account>&c to link your account."));
            return;
        }

        Optional<PlayerInfo> optionalPlayerInfo = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());

        // Player is not linked
        if (optionalPlayerInfo.isEmpty() || !optionalPlayerInfo.get().isVerified()) {
            // If player linking is not required for chat
            if (!plugin.getPluginConfig().isLinkRequired()) {
                e.setFormat(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPluginConfig().getPlayerFormatUnlinked()
                                .replaceAll("%username%", e.getPlayer().getName()))
                        // Message is replaced after colorized so message does not colorize
                        .replaceAll("%message%", e.getMessage()));

                if (messageRelay != null) {
                    WebhookMessageBuilder builder = new WebhookMessageBuilder();
                    builder.setUsername(e.getPlayer().getName());
                    // Set head image instead of Discord avatar
                    builder.setAvatarUrl(Util.getHeadURL(e.getPlayer().getUniqueId()));
                    builder.setContent(e.getMessage()
                            // Add a space after every @ in a message to prevent pings using webhook
                            .replaceAll("@", "@ "));
                    messageRelay.send(builder.build());
                }
            } else e.setCancelled(true);

            return;
        }

        PlayerInfo playerInfo = optionalPlayerInfo.get();

        if (!plugin.getPluginConfig().isChatEnabled()) return;

        if (plugin.getPluginConfig().isPlayerFormatEnabled()) {
            // Due to the nature of spigot chat messages it is not a good idea to disable the event and broadcast later
            // Luckily chat is handled on async threads anyway and members are cached after the first time so this is not that huge of a problem
            Member member = plugin.getBot().getGuild().retrieveMemberById(playerInfo.getDiscordID()).complete();

            e.setFormat(ChatColor.translateAlternateColorCodes('&',
                    plugin.getPluginConfig().getPlayerFormatLinked()
                            .replaceAll("%color%", Util.colorToChatString(
                                    member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                            .replaceAll("%username%", e.getPlayer().getName())
                            .replaceAll("%tag%", member.getUser().getAsTag()))
                    // Message is replaced after colorized so message does not colorize
                    .replaceAll("%message%", e.getMessage()));
        }

        if (messageRelay != null) {
            plugin.getBot().getJda().retrieveUserById(playerInfo.getDiscordID()).queue(user -> {
                WebhookMessageBuilder builder = new WebhookMessageBuilder();
                builder.setUsername(String.format("%s (%s)", e.getPlayer().getName(), user.getAsTag()));
                builder.setAvatarUrl(user.getAvatarUrl());
                builder.setContent(e.getMessage()
                        // Add a space after every @ in a message to prevent pings using webhook
                        .replaceAll("@", "@ "));
                messageRelay.send(builder.build());
            });
        }
    }
}