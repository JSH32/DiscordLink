package com.github.riku32.discordlink.core.bot.listeners;

import com.github.riku32.discordlink.core.Constants;
import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.core.database.Verification;
import com.github.riku32.discordlink.core.database.finders.PlayerInfoFinder;
import com.github.riku32.discordlink.core.database.finders.VerificationFinder;
import com.github.riku32.discordlink.core.framework.PlatformOfflinePlayer;
import com.github.riku32.discordlink.core.util.TextUtil;
import io.ebean.DB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;

public class CrossChatListener extends ListenerAdapter {
    private final DiscordLink plugin;
    private final Bot bot;

    public CrossChatListener(DiscordLink plugin, Bot bot) {
        this.plugin = plugin;
        this.bot = bot;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent e) {
        // Must be a member.
        if (e.getAuthor().isBot() || e.getAuthor().isSystem() || e.isWebhookMessage()) return;

        // Make sure message is in the cross-chat channel
        if (!e.getGuild().getId().equals(bot.getGuild().getId())) return;
        if (!e.getChannel().getId().equals(bot.getChannel().getId())) return;

        Member member = Objects.requireNonNull(e.getMember());
        Optional<PlayerInfo> playerInfo = new PlayerInfoFinder().byDiscordIdOptional(member.getId());

        if (plugin.getConfig().isLinkRequired()) {
            if (playerInfo.isEmpty()) {
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(new EmbedBuilder()
                                .setColor(Constants.Colors.FAIL)
                                .setTitle(plugin.getLocale().getElement("discord.not_linked.title").toString())
                                .setDescription(plugin.getLocale().getElement("discord.not_linked.description").toString())
                                .build())
                        .queue());
                e.getMessage().delete().queue();
                return;
            }

            if (!playerInfo.get().verified) {
                e.getAuthor().openPrivateChannel().queue(privateChannel -> {
                    Optional<Verification> optionalVerification = new VerificationFinder().byMember(playerInfo.get().discordId);
                    if (optionalVerification.isEmpty()) {
                        // Delete the player since the verification did not exist.
                        DB.delete(playerInfo);

                        privateChannel.sendMessageEmbeds(new EmbedBuilder()
                                .setColor(Constants.Colors.FAIL)
                                .setTitle(plugin.getLocale().getElement("discord.not_linked.title").toString())
                                .setDescription(plugin.getLocale().getElement("discord.not_linked.description").toString())
                                .build())
                                .queue();
                    } else {
                        Verification verification = optionalVerification.get();
                        privateChannel.retrieveMessageById(verification.verificationValue)
                                .queue(verificationMessage -> privateChannel.sendMessageEmbeds(new EmbedBuilder()
                                                .setColor(Constants.Colors.FAIL)
                                                .setTitle(plugin.getLocale().getElement("discord.not_verified.title").toString())
                                                .setDescription(plugin.getLocale().getElement("discord.not_verified.description")
                                                        .set("message_link", verificationMessage.getJumpUrl()).toString())
                                                .build())
                                        .queue());
                    }
                });

                e.getMessage().delete().queue();
                return;
            }
        } else if (playerInfo.isEmpty() || !playerInfo.get().verified) {
            plugin.broadcast(getMessage(e.getMessage(), null));
            return;
        }

        PlatformOfflinePlayer offlinePlayer = plugin.getPlugin().getOfflinePlayer(playerInfo.get().uuid);
        plugin.broadcast(getMessage(e.getMessage(), offlinePlayer));
    }

    private Component getMessage(Message message, PlatformOfflinePlayer offlinePlayer) {
        Member member = Objects.requireNonNull(message.getMember());

        // Remove all formatting which may exist
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(
                MiniMessage.miniMessage().deserialize(message.getContentStripped())
        );

        String format = offlinePlayer != null
                ? plugin.getConfig().getDiscordFormatLinked()
                : plugin.getConfig().getDiscordFormatUnlinked();

        return MiniMessage.miniMessage().deserialize(format
                .replaceAll("%color%", TextUtil.colorToHexMM(
                        member.getColor() == null ? Color.GRAY : member.getColor()))
                .replaceAll("%username%", offlinePlayer != null ? offlinePlayer.getName() : "")
                .replaceAll("%tag%", member.getUser().getAsTag())
                // Message is replaced after colorized so message does not colorize
                .replaceAll("%message%", message.getAttachments().isEmpty()
                        ? plainMessage
                        : String.format("%s<dark_gray>%d attachments", plainMessage + " ", message.getAttachments().size()))
                // Minecraft chat doesn't allow newlines so all discord message newlines will be removed
                .replaceAll("\n", ""));
    }
}
