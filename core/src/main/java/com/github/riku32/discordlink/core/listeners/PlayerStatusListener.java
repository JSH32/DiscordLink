package com.github.riku32.discordlink.core.listeners;

import com.github.riku32.discordlink.core.config.Config;
import com.github.riku32.discordlink.core.Constants;
import com.github.riku32.discordlink.core.framework.PlatformPlugin;
import com.github.riku32.discordlink.core.framework.eventbus.events.PlayerDeathEvent;
import com.github.riku32.discordlink.core.util.SkinUtil;
import com.github.riku32.discordlink.core.util.TextUtil;
import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.framework.dependency.annotation.Dependency;
import com.github.riku32.discordlink.core.framework.eventbus.annotation.EventHandler;
import com.github.riku32.discordlink.core.framework.eventbus.events.PlayerJoinEvent;
import com.github.riku32.discordlink.core.framework.GameMode;
import com.github.riku32.discordlink.core.framework.eventbus.events.PlayerQuitEvent;
import com.github.riku32.discordlink.core.locale.Locale;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Optional;
import java.util.Set;

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
                event.getPlayer().sendMessage(TextUtil.colorize(locale.getElement("join.verify_link")
                                .set("user_tag", user.getAsTag())
                                .set("bot_tag", bot.getJda().getSelfUser().getAsTag())
                                .info()));

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

            if (config.isChannelBroadcastJoin()) {
                bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Constants.Colors.SUCCESS)
                        .setAuthor(event.getPlayer().getName() + " has joined", null, SkinUtil.getHeadURL(event.getPlayer().getUuid()))
                        .build())
                        .queue();
            }

            if (config.isStatusEnabled()) {
                String joinMessage = TextUtil.colorize(config.getStatusJoinUnlinked()
                        .replaceAll("%username%", event.getPlayer().getName()));
                event.setJoinMessage(joinMessage);
            }

            return;
        }

        bot.getJda().retrieveUserById((playerInfoOptional.get().discordId)).queue(user -> {
            Guild guild = bot.getGuild();
            guild.retrieveMemberById(playerInfoOptional.get().discordId).queue(
                member -> {
                    if (config.isStatusEnabled()) {
                        platform.broadcast(TextUtil.colorize(config.getStatusJoinLinked()
                                .replaceAll("%username%", event.getPlayer().getName())
                                .replaceAll("%tag%", user.getAsTag()))
                                .replaceAll("%color%", member.getColor() != null ?
                                        TextUtil.colorToChatString(member.getColor()) : "&7"));
                    }

                    event.getPlayer().setGameMode(platform.getDefaultGameMode());

                    if (config.isChannelBroadcastJoin()) {
                        bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                                .setColor(Constants.Colors.SUCCESS)
                                .setAuthor(String.format("%s (%s) has joined", event.getPlayer().getName(), user.getAsTag()),
                                        null, user.getAvatarUrl())
                                .build())
                                .queue();
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

                    event.getPlayer().kickPlayer(TextUtil.colorize(
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
                    platform.broadcast(TextUtil.colorize(
                            config.getStatusQuitLinked()
                                    .replaceAll("%username%", event.getPlayer().getName())
                                    .replaceAll("%tag%", member.getUser().getAsTag())
                                    .replaceAll("%color%", member.getColor() != null ?
                                            TextUtil.colorToChatString(member.getColor()) : "&7")
                    ));
                }

                if (config.isChannelBroadcastQuit()) {
                    bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(Constants.Colors.FAIL)
                            .setAuthor(String.format("%s (%s) has left", event.getPlayer().getName(), member.getUser().getAsTag()),
                                    null, member.getUser().getAvatarUrl())
                            .build())
                            .queue();
                }
            });
        } else if (!config.isLinkRequired()) {
            if (config.isStatusEnabled()) {
                event.setQuitMessage(TextUtil.colorize(config.getStatusQuitUnlinked()
                        .replaceAll("%username%", event.getPlayer().getName())));
            }

            if (config.isChannelBroadcastQuit()) {
                bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Constants.Colors.FAIL)
                        .setAuthor(String.format("%s has left", event.getPlayer().getName()), null, SkinUtil.getHeadURL(event.getPlayer().getUuid()))
                        .build())
                        .queue();
            }
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        final String causeWithoutName;
        if (event.getDeathMessage() == null)
            causeWithoutName = "died";
        else
            causeWithoutName = event.getDeathMessage().substring(event.getDeathMessage().indexOf(" ") + 1).replaceAll("\n", "");

        // Disable default event if status broadcast is enabled
        if (config.isStatusEnabled())
            event.setDeathMessage(null);

        Optional<PlayerInfo> playerInfoOptional = PlayerInfo.find.byUuidOptional(event.getPlayer().getUuid());
        if (playerInfoOptional.isPresent() && playerInfoOptional.get().verified) {
            bot.getGuild().retrieveMemberById((playerInfoOptional.get().discordId)).queue(member -> {
                // Send custom death message if status is enabled, else handle normally
                if (config.isStatusEnabled()) {
                    platform.broadcast(TextUtil.colorize(
                            config.getStatusDeathLinked()
                                    .replaceAll("%username%", event.getPlayer().getName())
                                    .replaceAll("%tag%", member.getUser().getAsTag())
                                    .replaceAll("%cause%", causeWithoutName))
                                    .replaceAll("%color%", member.getColor() != null ?
                                            TextUtil.colorToChatString(member.getColor()) : "&7"));
                }

                if (config.isChannelBroadcastDeath()) {
                    bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(Constants.Colors.FAIL)
                            .setAuthor(String.format("%s (%s) %s", event.getPlayer().getName(), member.getUser().getAsTag(), causeWithoutName),
                                    null, member.getUser().getAvatarUrl())
                            .build())
                            .queue();
                }
            });
        } else if (!config.isLinkRequired()) {
            if (config.isStatusEnabled()) {
                event.setDeathMessage(TextUtil.colorize(
                        config.getStatusDeathUnlinked()
                                .replaceAll("%username%", event.getPlayer().getName())
                                .replaceAll("%cause%", causeWithoutName)));
            }

            if (config.isChannelBroadcastDeath()) {
                bot.getChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Constants.Colors.FAIL)
                        .setAuthor(String.format("%s %s", event.getPlayer().getName(), causeWithoutName), null, SkinUtil.getHeadURL(event.getPlayer().getUuid()))
                        .build())
                        .queue();
            }
        }
    }
}
