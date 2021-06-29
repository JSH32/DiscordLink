package com.github.riku32.discordlink.events;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.riku32.discordlink.DiscordLink;
import com.github.riku32.discordlink.PlayerInfo;
import com.github.riku32.discordlink.Util;
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
        // Cancel if player is frozen for whatever reason, takes less resources than database statement below
        if (plugin.getFrozenPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        Optional<PlayerInfo> optionalPlayerInfo = plugin.getDatabase().getPlayerInfo(e.getPlayer().getUniqueId());
        if (!optionalPlayerInfo.isPresent() || !optionalPlayerInfo.get().isVerified()) {
            e.setCancelled(true);
            return;
        }

        PlayerInfo playerInfo = optionalPlayerInfo.get();

        if (!plugin.getPluginConfig().isChatEnabled()) return;

        // Due to the nature of spigot chat messages it is not a good idea to disable the event and broadcast later
        // Luckily chat is handled on async threads anyway and members are cached after the first time so this is not that huge of a problem
        Member member = plugin.getBot().getGuild().retrieveMemberById(playerInfo.getDiscordID()).complete();

        e.setFormat(ChatColor.translateAlternateColorCodes('&',
                plugin.getPluginConfig().getPlayerFormat()
                        .replaceAll("%color%", Util.colorToChatString(
                                member.getColor() != null ? member.getColor() : ChatColor.GRAY.getColor()))
                        .replaceAll("%username%", e.getPlayer().getName())
                        .replaceAll("%tag%", member.getUser().getAsTag()))
                // Message is replaced after colorized so message does not colorize
                .replaceAll("%message%", e.getMessage()));

        if (messageRelay != null) {
            plugin.getBot().getJda().retrieveUserById(playerInfo.getDiscordID()).queue(user -> {
                WebhookMessageBuilder builder = new WebhookMessageBuilder();
                builder.setUsername(String.format("%s (%s)", user.getName(),
                        plugin.getServer().getOfflinePlayer(playerInfo.getUuid()).getName()));
                builder.setAvatarUrl(user.getAvatarUrl());
                builder.setContent(e.getMessage()
                        // Add a space after every @ in a message to prevent pings using webhook
                        .replaceAll("@", "@ "));
                messageRelay.send(builder.build());
            });
        }
    }
}