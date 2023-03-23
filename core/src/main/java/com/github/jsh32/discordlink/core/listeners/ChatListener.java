package com.github.jsh32.discordlink.core.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.jsh32.discordlink.core.DiscordLink;
import com.github.jsh32.discordlink.core.bot.Bot;
import com.github.jsh32.discordlink.core.database.PlayerInfo;
import com.github.jsh32.discordlink.core.framework.eventbus.annotation.EventHandler;
import com.github.jsh32.discordlink.core.framework.eventbus.events.PlayerChatEvent;
import com.github.jsh32.discordlink.core.util.SkinUtil;
import com.github.jsh32.discordlink.core.util.TextUtil;
import net.dv8tion.jda.api.entities.Member;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.awt.*;
import java.util.Optional;

/**
 * Player chat event handler.
 */
public class ChatListener {
    private final DiscordLink plugin;
    private final WebhookClient messageRelay;
    private final Bot bot;

    /**
     * @param plugin DiscordLink plugin instance
     * @param messageRelay webhook to send in-game messages to, if {@code null} messages will not be relayed
     */
    public ChatListener(DiscordLink plugin, WebhookClient messageRelay, Bot bot) {
        this.plugin = plugin;
        this.messageRelay = messageRelay;
        this.bot = bot;
    }

    @EventHandler
    private void onPlayerChat(PlayerChatEvent e) {
        // Cancel if player is frozen for whatever reason
        if (plugin.getFrozenPlayers().contains(e.getPlayer())) {
            e.setCancelled(true);
            if (plugin.getConfig().isLinkRequired())
                e.getPlayer().sendMessage(plugin.getLocale().getElement("link.required_notify").info());
            return;
        }

        // TODO: Replace this with something else
        // This removes message formatting.
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(
                MiniMessage.miniMessage().deserialize(PlainTextComponentSerializer.plainText().serialize(e.getMessage()))
        );

        Optional<PlayerInfo> playerInfoOptional = PlayerInfo.find.byUuidOptional(e.getPlayer().getUuid());

        // Player is not linked
        if (playerInfoOptional.isEmpty() || !playerInfoOptional.get().verified) {
            // If player linking is not required for chat
            if (!plugin.getConfig().isLinkRequired()) {
                e.setMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getPlayerFormatUnlinked()
                        .replaceAll("%username%", e.getPlayer().getName())
                        .replaceAll("%message%", plainMessage)));

                if (messageRelay != null) {
                    WebhookMessageBuilder builder = new WebhookMessageBuilder();
                    builder.setUsername(e.getPlayer().getName());
                    // Set head image instead of Discord avatar
                    builder.setAvatarUrl(SkinUtil.getHeadURL(e.getPlayer().getUuid()));
                    builder.setContent(plainMessage
                            // Add a space after every @ in a message to prevent pings using webhook
                            .replaceAll("@", "@ "));
                    messageRelay.send(builder.build());
                }
            } else e.setCancelled(true);

            return;
        }

        PlayerInfo playerInfo = playerInfoOptional.get();
        if (!plugin.getConfig().isChatEnabled()) return;


        if (plugin.getConfig().isPlayerFormatEnabled()) {
            // Due to the nature of chat events on some platforms, it is not a good idea to disable the event and broadcast later
            // Luckily chat is handled on async threads anyway and members are cached after the first time so this is not that huge of a problem
            Member member = bot.getGuild().retrieveMemberById(playerInfo.discordId).complete();

            e.setMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getConfig().getPlayerFormatLinked()
                            .replaceAll("%color%", TextUtil.colorToHexMM(
                                    member.getColor() != null ? member.getColor() : Color.GRAY))
                            .replaceAll("%username%", e.getPlayer().getName())
                            .replaceAll("%tag%", member.getUser().getAsTag())
                            // Message is replaced after colorized so message does not colorize
                            .replaceAll("%message%", plainMessage))
            );


            if (messageRelay != null) {
                bot.getJda().retrieveUserById(playerInfo.discordId).queue(user -> {
                    WebhookMessageBuilder builder = new WebhookMessageBuilder();
                    builder.setUsername(String.format("%s (%s)", e.getPlayer().getName(), user.getAsTag()));
                    builder.setAvatarUrl(user.getAvatarUrl());
                    builder.setContent(plainMessage
                            // Add a space after every @ in a message to prevent pings using webhook
                            .replaceAll("@", "@ "));
                    messageRelay.send(builder.build());
                });
            }
        }
    }
}
