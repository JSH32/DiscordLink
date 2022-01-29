package com.github.riku32.discordlink.core.bot.listeners;

import com.github.riku32.discordlink.core.Constants;
import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.database.DataException;
import com.github.riku32.discordlink.core.database.managers.PlayerManager;
import com.github.riku32.discordlink.core.database.model.PlayerIdentity;
import com.github.riku32.discordlink.core.database.model.PlayerInfo;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.util.MojangAPI;
import com.github.riku32.discordlink.core.util.TextUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class VerificationListener extends ListenerAdapter {
    private final Bot bot;
    private final DiscordLink plugin;
    private final PlayerManager playerManager;
    private final MojangAPI mojangAPI = new MojangAPI();

    public VerificationListener(Bot bot, DiscordLink plugin) {
        this.bot = bot;
        this.plugin = plugin;
        this.playerManager = plugin.getDatabase();
    }

    public void onButtonClick(ButtonClickEvent event) {
        if (event.getChannelType().isGuild()) return;
        if (!event.getComponentId().startsWith("link.")) return;

        bot.getGuild().retrieveMemberById(event.getUser().getId()).queue(member -> {
            Objects.requireNonNull(event.getMessage()).editMessage(new MessageBuilder()
                    .setContent(" ")
                    .setActionRows(ActionRow.of(event.getMessage().getButtons().stream().map(Button::asDisabled).collect(Collectors.toList())
                    )).build()).queue();

            PlayerInfo playerInfo;
            try {
                var optionalPlayerInfo = playerManager.getPlayerInfo(PlayerIdentity.from(event.getUser().getId()));

                // If the player didn't exist or was verified then something weird happened
                // Just acknowledge the interaction and ignore since the ActionRows already greyed
                if (optionalPlayerInfo.isEmpty()) {
                    event.deferEdit().queue();
                    return;
                }

                playerInfo = optionalPlayerInfo.get();
                if (playerInfo.isVerified()) {
                    event.deferEdit().queue();
                    return;
                }
            } catch (DataException exception) {
                exception.printStackTrace();
                return;
            }


            try {
                if (!playerManager.getVerificationMessage(PlayerIdentity.from(playerInfo.getDiscordID())).equals(event.getMessageId()))
                    return;
            } catch (DataException exception) {
                exception.printStackTrace();
                return;
            }

            mojangAPI.getName(playerInfo.getUuid(), name -> {
                switch (event.getComponentId()) {
                    case "link.verify": {
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("Linked")
                                .setDescription(String.format("Your discord account has been linked to %s", name))
                                .setColor(Constants.Colors.SUCCESS)
                                .build()).queue();

                        try {
                            playerManager.verifyPlayer(PlayerIdentity.from(playerInfo.getUuid()));
                        } catch (DataException exception) {
                            exception.printStackTrace();
                        }

                        PlatformPlayer player = plugin.getPlugin().getPlayer(playerInfo.getUuid());
                        if (player != null) {
                            player.sendMessage(TextUtil.colorize(String.format("&7Your minecraft account has been linked to &e%s", event.getUser().getAsTag())));

                            plugin.getFrozenPlayers().remove(player);

                            if (plugin.getConfig().isLinkRequired()) {
                                if (plugin.getConfig().isStatusEnabled()) {
                                    plugin.getPlugin().broadcast(TextUtil.colorize(plugin.getConfig().getStatusJoinLinked()
                                            .replaceAll("%username%", name)
                                            .replaceAll("%tag%", event.getUser().getAsTag())
                                            .replaceAll("%color%", member.getColor() != null ?
                                                    TextUtil.colorToChatString(member.getColor()) : "&7")));
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

                        PlatformPlayer player = plugin.getPlugin().getPlayer(playerInfo.getUuid());
                        if (player != null)
                            player.sendMessage(TextUtil.colorize(String.format("&e%s&7 has cancelled the linking process", event.getUser().getAsTag())));

                        try {
                            playerManager.deletePlayer(PlayerIdentity.from(playerInfo.getUuid()));
                        } catch (DataException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }, null);
        });
    }
}
