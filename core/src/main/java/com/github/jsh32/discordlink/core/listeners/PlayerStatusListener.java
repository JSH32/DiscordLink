package com.github.jsh32.discordlink.core.listeners;

import com.github.jsh32.discordlink.core.Constants;
import com.github.jsh32.discordlink.core.bot.Bot;
import com.github.jsh32.discordlink.core.config.Config;
import com.github.jsh32.discordlink.core.database.PlayerInfo;
import com.github.jsh32.discordlink.core.framework.GameMode;
import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.PlatformPlugin;
import com.github.jsh32.discordlink.core.framework.dependency.annotation.Dependency;
import com.github.jsh32.discordlink.core.framework.eventbus.annotation.EventHandler;
import com.github.jsh32.discordlink.core.framework.eventbus.events.PlayerDeathEvent;
import com.github.jsh32.discordlink.core.framework.eventbus.events.PlayerJoinEvent;
import com.github.jsh32.discordlink.core.framework.eventbus.events.PlayerQuitEvent;
import com.github.jsh32.discordlink.core.locale.Locale;
import com.github.jsh32.discordlink.core.util.SkinUtil;
import com.github.jsh32.discordlink.core.util.TextUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlayerStatusListener {
    @Dependency
    private PlatformPlugin platform;

    @Dependency
    private Config config;

    @Dependency
    private Locale locale;

    @Dependency
    private Bot bot;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Dependency(named = "frozenPlayers")
    private Set<PlatformPlayer> frozenPlayers;

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (config.isStatusEnabled()) event.setJoinMessage(null);

        Optional<PlayerInfo> playerInfoOptional = PlayerInfo.find.byUuidOptional(event.getPlayer().getUuid());
        if (playerInfoOptional.isEmpty()) {
            if (config.isLinkRequired()) {
                event.getPlayer().sendMessage(locale.getElement("join.unregistered").info());
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                frozenPlayers.add(event.getPlayer());
            }
        } else if (!playerInfoOptional.get().verified) {
            bot.getJda().retrieveUserById((playerInfoOptional.get().discordId)).queue(user -> {
                event.getPlayer().sendMessage(locale.getElement("join.verify_link")
                                .set("user_tag", user.getAsTag())
                                .set("bot_tag", bot.getJda().getSelfUser().getAsTag())
                                .info());

                if (config.isLinkRequired()) {
                    frozenPlayers.add(event.getPlayer());
                    event.getPlayer().setGameMode(GameMode.SPECTATOR);
                }
            }, ignored -> {
                // User is invalid/left before verification, just remove the data that was leftover
                playerInfoOptional.get().delete();
            });
        }

        // If player is not linked
        if (playerInfoOptional.isEmpty() || !playerInfoOptional.get().verified) {
            if (config.isLinkRequired()) return;

            if (config.isChannelBroadcastJoin())
                sendUnlinkedEventToChat(event.getPlayer().getUuid(), true, event.getPlayer().getName() + " has joined");

            if (config.isStatusEnabled()) {
                event.setJoinMessage(MiniMessage.miniMessage().deserialize(config.getStatusJoinUnlinked()
                        .replaceAll("%username%", event.getPlayer().getName())));
            }

            return;
        }

        bot.getJda().retrieveUserById((playerInfoOptional.get().discordId)).queue(user -> {
            Guild guild = bot.getGuild();
            guild.retrieveMemberById(playerInfoOptional.get().discordId).queue(
                member -> {
                    if (config.isStatusEnabled()) {
                        platform.broadcast(MiniMessage.miniMessage().deserialize(config.getStatusJoinLinked()
                                .replaceAll("%username%", event.getPlayer().getName())
                                .replaceAll("%tag%", user.getAsTag())
                                .replaceAll("%color%", member.getColor() != null ?
                                        TextUtil.colorToHexMM(member.getColor()) : "<gray>")));
                    }

                    event.getPlayer().setGameMode(platform.getDefaultGameMode());

                    if (config.isChannelBroadcastJoin()) {
                        sendLinkedEventToChat(member, true,
                                String.format("%s (%s) has joined", event.getPlayer().getName(), user.getAsTag()));
                    }
                },
                ignored -> {
                    if (config.isAllowUnlink()) {
                        playerInfoOptional.get().delete();
                        event.getPlayer().sendMessage(locale.getElement("join.left_server").error());

                        if (config.isLinkRequired()) {
                            frozenPlayers.add(event.getPlayer());
                            event.getPlayer().setGameMode(GameMode.SPECTATOR);
                            event.getPlayer().sendMessage(locale.getElement("link.link").info());
                        }

                        return;
                    }

                    event.getPlayer().kickPlayer(MiniMessage.miniMessage().deserialize(
                            config.getKickNotInGuild().replaceAll("%tag%", user.getAsTag())));
                }
            );
        });
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        frozenPlayers.remove(event.getPlayer());

        // Do not send default leave message
        if (config.isStatusEnabled()) event.setQuitMessage(null);

        Optional<PlayerInfo> playerInfoOptional = PlayerInfo.find.byUuidOptional(event.getPlayer().getUuid());
        if (playerInfoOptional.isPresent() && playerInfoOptional.get().verified) {
            PlayerInfo playerInfo = playerInfoOptional.get();
            bot.getGuild().retrieveMemberById(playerInfo.discordId).queue(member -> {
                if (config.isStatusEnabled()) {
                    platform.broadcast(MiniMessage.miniMessage().deserialize(
                            config.getStatusQuitLinked()
                                    .replaceAll("%username%", event.getPlayer().getName())
                                    .replaceAll("%tag%", member.getUser().getAsTag())
                                    .replaceAll("%color%", member.getColor() != null ?
                                            TextUtil.colorToHexMM(member.getColor()) : "<gray>")));
                }

                if (config.isChannelBroadcastQuit())
                    sendLinkedEventToChat(member, false,
                            String.format("%s (%s) has left", event.getPlayer().getName(), member.getUser().getAsTag()));
            });
        } else if (!config.isLinkRequired()) {
            if (config.isStatusEnabled()) {
                event.setQuitMessage(MiniMessage.miniMessage().deserialize(config.getStatusQuitUnlinked()
                        .replaceAll("%username%", event.getPlayer().getName())));
            }

            if (config.isChannelBroadcastQuit())
                sendUnlinkedEventToChat(event.getPlayer().getUuid(), false, String.format("%s has left", event.getPlayer().getName()));
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        final String causeWithoutName;
        if (event.getDeathMessage() == null) {
            causeWithoutName = "died";
        } else {
            String deathMessage = PlainTextComponentSerializer.plainText().serialize(event.getDeathMessage());
            causeWithoutName = deathMessage.substring(deathMessage.indexOf(" ") + 1).replaceAll("\n", "");
        }

        // Disable default event if status broadcast is enabled
        if (config.isStatusEnabled())
            event.setDeathMessage(null);

        Optional<PlayerInfo> playerInfoOptional = PlayerInfo.find.byUuidOptional(event.getPlayer().getUuid());
        if (playerInfoOptional.isPresent() && playerInfoOptional.get().verified) {
            bot.getGuild().retrieveMemberById((playerInfoOptional.get().discordId)).queue(member -> {
                // Send custom death message if status is enabled, else handle normally
                if (config.isStatusEnabled()) {
                    platform.broadcast(MiniMessage.miniMessage().deserialize(
                                    config.getStatusDeathLinked()
                                            .replaceAll("%username%", event.getPlayer().getName())
                                            .replaceAll("%tag%", member.getUser().getAsTag())
                                            .replaceAll("%cause%", causeWithoutName)
                                            .replaceAll("%color%", member.getColor() != null ?
                                                    TextUtil.colorToHexMM(member.getColor()) : "<gray>")));
                }

                if (config.isChannelBroadcastDeath())
                    sendLinkedEventToChat(member, false,
                            String.format("%s (%s) %s", event.getPlayer().getName(), member.getUser().getAsTag(), causeWithoutName));
            });
        } else if (!config.isLinkRequired()) {
            if (config.isStatusEnabled()) {
                event.setDeathMessage(MiniMessage.miniMessage().deserialize(
                        config.getStatusDeathUnlinked()
                                .replaceAll("%username%", event.getPlayer().getName())
                                .replaceAll("%cause%", causeWithoutName)));
            }

            if (config.isChannelBroadcastDeath())
                sendUnlinkedEventToChat(event.getPlayer().getUuid(), false, String.format("%s %s", event.getPlayer().getName(), causeWithoutName));
        }
    }

    private void sendLinkedEventToChat(Member member, boolean success, String text) {
        bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(success ? Constants.Colors.SUCCESS : Constants.Colors.FAIL)
                        .setAuthor(text, null, member.getUser().getAvatarUrl())
                        .build())
                .queue();
    }

    private void sendUnlinkedEventToChat(UUID uuid, boolean success, String text) {
        bot.getChannel().sendMessage(new MessageBuilder().setEmbeds(new EmbedBuilder()
                        .setColor(success ? Constants.Colors.SUCCESS : Constants.Colors.FAIL)
                        .setAuthor(text, null, SkinUtil.getHeadURL(uuid))
                        .build()).build())
                .submit();
    }
}
