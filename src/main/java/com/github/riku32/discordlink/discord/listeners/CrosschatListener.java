package com.github.riku32.discordlink.discord.listeners;

import com.github.riku32.discordlink.Constants;
import com.github.riku32.discordlink.DiscordLink;
import com.github.riku32.discordlink.PlayerInfo;
import com.github.riku32.discordlink.Util;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class CrosschatListener extends ListenerAdapter {
    private final DiscordLink plugin;

    public CrosschatListener(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @SneakyThrows
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem() || event.isWebhookMessage()) return;

        // Make sure message is in both the same guild and channel before proceeding
        if (!event.getGuild().getId().equals(plugin.getBot().getGuild().getId())) return;
        if (!event.getChannel().getId().equals(plugin.getBot().getChannel().getId())) return;

        Member member = Objects.requireNonNull(event.getMember());

        Optional<PlayerInfo> optionalPlayerInfo = plugin.getDatabase().getPlayerInfo(event.getAuthor().getId());
        if (plugin.getPluginConfig().isLinkRequired()) {
            if (!optionalPlayerInfo.isPresent()) {
                event.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(new EmbedBuilder()
                        .setColor(Constants.Colors.FAIL)
                        .setTitle("Not linked")
                        .setDescription("Your can't use crosschat unless you link your Minecraft account. " +
                                "Please join the server and type `/link` to start the link process.")
                        .build())
                        .queue());
                event.getMessage().delete().queue();
                return;
            }

            if (!optionalPlayerInfo.get().isVerified()) {
                event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                    try {
                        privateChannel.retrieveMessageById(plugin.getDatabase().getMessageId(optionalPlayerInfo.get().getDiscordID()))
                                .queue(verificationMessage -> privateChannel.sendMessage(new EmbedBuilder()
                                    .setColor(Constants.Colors.FAIL)
                                    .setTitle("Not verified")
                                    .setDescription(String.format("Your can't use crosschat unless you verify your Minecraft account link. " +
                                            "Please press either the verify or cancel buttons on the [verification message](%s).", verificationMessage.getJumpUrl()))
                                    .build())
                                    .queue());
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                });
                event.getMessage().delete().queue();
                return;
            }
        } else if (!optionalPlayerInfo.isPresent() || !optionalPlayerInfo.get().isVerified()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getPluginConfig().getDiscordFormatUnlinked()
                            .replaceAll("%color%", Util.colorToChatString(
                                    member.getColor() == null ? ChatColor.GRAY.getColor() : member.getColor()))
                            .replaceAll("%tag%", member.getUser().getAsTag()))
                    // Message is replaced after colorized so message does not colorize
                    .replaceAll("%message%",
                            event.getMessage().getContentStripped().isEmpty()
                                    ? ChatColor.DARK_GRAY + String.format("%d attachments", event.getMessage().getAttachments().size())
                                    : event.getMessage().getContentStripped())
                    // Minecraft chat doesnt allow newlines so all discord message newlines will be removed
                    .replaceAll("\n", ""));
            return;
        }

        PlayerInfo playerInfo = optionalPlayerInfo.get();

        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerInfo.getUuid());
        if (!offlinePlayer.hasPlayedBefore()) {
            // Something went wrong for it to go this far
            event.getMessage().delete().queue();
            return;
        }

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getPluginConfig().getDiscordFormatLinked()
                        .replaceAll("%color%", Util.colorToChatString(
                                member.getColor() == null ? ChatColor.GRAY.getColor() : member.getColor()))
                        .replaceAll("%username%", Objects.requireNonNull(offlinePlayer.getName()))
                        .replaceAll("%tag%", member.getUser().getAsTag()))
                // Message is replaced after colorized so message does not colorize
                .replaceAll("%message%",
                        event.getMessage().getContentStripped().isEmpty()
                                ? ChatColor.DARK_GRAY + String.format("%d attachments", event.getMessage().getAttachments().size())
                                : event.getMessage().getContentStripped())
                // Minecraft chat doesnt allow newlines so all discord message newlines will be removed
                .replaceAll("\n", ""));
    }
}
