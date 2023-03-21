package com.github.riku32.discordlink.core.bot.listeners;

import com.github.riku32.discordlink.core.Constants;
import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.database.Verification;
import com.github.riku32.discordlink.core.database.enums.VerificationType;
import com.github.riku32.discordlink.core.framework.PlatformOfflinePlayer;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.locale.Locale;
import com.github.riku32.discordlink.core.util.MojangAPI;
import com.github.riku32.discordlink.core.util.TextUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Objects;
import java.util.stream.Collectors;

public class VerificationListener extends ListenerAdapter {
    private final Bot bot;
    private final DiscordLink plugin;
    private final MojangAPI mojangAPI = new MojangAPI();

    private final Locale locale;

    public VerificationListener(Bot bot, DiscordLink plugin) {
        this.bot = bot;
        this.plugin = plugin;
        this.locale = plugin.getLocale();
    }

    public void onButtonClick(ButtonClickEvent event) {
        if (event.getChannelType().isGuild()) return;
        if (!event.getComponentId().startsWith("link.")) return;

        bot.getGuild().retrieveMemberById(event.getUser().getId()).queue(member -> {
            Objects.requireNonNull(event.getMessage()).editMessage(new MessageBuilder()
                    .setContent(" ")
                    .setActionRows(ActionRow.of(event.getMessage().getButtons().stream().map(Button::asDisabled).collect(Collectors.toList())
                    )).build()).queue();

            Verification verification;
            var optionalVerification = Verification.find.byValueAndType(VerificationType.MESSAGE_REACTION, event.getMessageId());

            // If the player didn't exist or was verified then something weird happened
            // Just acknowledge the interaction and ignore since the ActionRows already greyed
            if (optionalVerification.isEmpty()) {
                event.deferEdit().queue();
                return;
            }

            verification = optionalVerification.get();

            // If the player was verified we should delete the verification, it should not exist
            if (verification.player.verified) {
                verification.delete();
                event.deferEdit().queue();
                return;
            }

            if (!verification.verificationValue.equals(event.getMessageId()))
                return;

            PlatformOfflinePlayer offlinePlayer = plugin.getPlugin().getOfflinePlayer(verification.player.uuid);

            switch (event.getComponentId()) {
                case "link.verify": {
                    event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Linked")
                        .setDescription(String.format("Your discord account has been linked to %s", offlinePlayer.getName()))
                        .setColor(Constants.Colors.SUCCESS)
                        .build()).queue();

                    // Verify the player
                    verification.player
                        .setVerified(true)
                        .save();

                    if (offlinePlayer.isOnline()) {
                        PlatformPlayer player = plugin.getPlugin().getPlayer(verification.player.uuid);
                        player.sendMessage(locale.getElement("verify.linked")
                                .set("tag", event.getUser().getAsTag()).success());
                        verification.delete(); // Delete the verification once we verify

                        plugin.getFrozenPlayers().remove(player);

                        if (plugin.getConfig().isLinkRequired()) {
                            if (plugin.getConfig().isStatusEnabled()) {
                                plugin.getPlugin().broadcast(MiniMessage.miniMessage().deserialize(
                                        plugin.getConfig().getStatusJoinLinked()
                                                .replaceAll("%username%", player.getName())
                                                .replaceAll("%tag%", event.getUser().getAsTag())
                                                .replaceAll("%color%", member.getColor() != null ?
                                                        TextUtil.colorToHexMM(member.getColor()) : "<gray>"))
                                );
                            }

                            if (plugin.getConfig().isCrossChatEnabled()) {
                                if (bot.getChannel() != null)
                                    bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                                            .setColor(Constants.Colors.SUCCESS)
                                            .setAuthor(String.format("%s (%s) has joined", player.getName(), event.getUser().getAsTag()),
                                                null, event.getUser().getAvatarUrl())
                                            .build())
                                        .queue();
                            }

                            player.setGameMode(plugin.getPlugin().getDefaultGameMode());
                        }
                    }

                    break;
                }
                case "link.cancel": {
                    event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Cancelled")
                        .setDescription("You have cancelled the linking process")
                        .setColor(Constants.Colors.FAIL)
                        .build()).queue();

                    PlatformPlayer player = plugin.getPlugin().getPlayer(verification.player.uuid);
                    if (player != null)
                        player.sendMessage(locale.getElement("verify.cancelled")
                                .set("tag", event.getUser().getAsTag()).error());

                    verification.player.delete();
                }
            }
        });
    }
}
