package com.github.riku32.discordlink.core.commands;

import com.github.riku32.discordlink.core.Config;
import com.github.riku32.discordlink.core.Constants;
import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.database.DataException;
import com.github.riku32.discordlink.core.database.managers.PlayerManager;
import com.github.riku32.discordlink.core.database.model.PlayerIdentity;
import com.github.riku32.discordlink.core.database.model.PlayerInfo;
import com.github.riku32.discordlink.core.framework.command.CommandSender;
import com.github.riku32.discordlink.core.framework.command.annotation.Command;
import com.github.riku32.discordlink.core.framework.command.annotation.Default;
import com.github.riku32.discordlink.core.framework.dependency.annotation.Dependency;
import com.github.riku32.discordlink.core.locale.Locale;
import com.github.riku32.discordlink.core.util.SkinUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Command(aliases = {"link"}, userOnly = true)
public class CommandLink {
    @Dependency
    private PlayerManager playerManager;

    @Dependency
    private Bot bot;

    @Dependency
    private Config config;

    @Dependency
    private Locale locale;

    @Default
    private boolean link(CommandSender sender, String tag) throws DataException, IOException {
        Optional<PlayerInfo> playerInfoOptional = playerManager.getPlayerInfo(PlayerIdentity.from(sender.getPlayer().getUuid()));
        if (playerInfoOptional.isPresent()) {
            PlayerInfo playerInfo = playerInfoOptional.get();

            bot.getJda().retrieveUserById(playerInfo.getDiscordID()).queue(user -> {
                if (playerInfo.isVerified()) {
                    sender.sendMessage((config.isAllowUnlink() ?
                            locale.getElement("link.already_linked")
                            : locale.getElement("link.already_linked_unlink"))
                            .set("tag", user.getAsTag()).error());
                } else {
                    sender.sendMessage(locale.getElement("link.in_process")
                            .set("tag", user.getAsTag()).error());
                }
            });

            return false;
        }

        Member member;
        try {
            member = bot.getGuild().getMemberByTag(tag);
        } catch (IllegalArgumentException ignored) {
            sender.sendMessage(locale.getElement("link.account_invalid").error());
            return false;
        }

        if (member == null) {
            sender.sendMessage(locale.getElement("link.account_invalid").error());
            return false;
        }

        if (playerManager.isPlayerLinked(PlayerIdentity.from(member.getId()))) {
            sender.sendMessage(locale.getElement("link.linked").error());
            return false;
        }

        Message verificationMessage = new MessageBuilder()
                .setEmbeds(new EmbedBuilder()
                        .setColor(Constants.Colors.SUCCESS)
                        .setTitle("Minecraft Link")
                        .setThumbnail(SkinUtil.getIsometricHeadStream(sender.getUniqueId()))
                        .setDescription("Minecraft to Discord link initiated. Press verify to complete the account link process. If you do not want to link accounts or this was not you, press cancel." +
                                (!config.isAllowUnlink() ? "\n\n\u26A0 **THIS CANNOT BE UNDONE**" : ""))
                        .addField("Username", sender.getName(), true)
                        .addField("UUID", sender.getPlayer().getUuid().toString(), true)
                        .build())
                .setActionRows(ActionRow.of(
                        Button.success("link.verify", "Verify"),
                        Button.danger("link.cancel", "Cancel")
                ))
                .build();

        member.getUser().openPrivateChannel().submit()
                .thenCompose(privateChannel -> privateChannel.sendMessage(verificationMessage)
                        .submit())
                .whenComplete((message, error) -> {
                    if (error != null) {
                        sender.sendMessage(locale.getElement("link.dm_disabled").error());
                        return;
                    }

                    try {
                        playerManager.createPlayer(sender.getUniqueId(), member.getId(), message.getId());
                    } catch (DataException e) {
                        e.printStackTrace();
                    }

                    sender.sendMessage(locale.getElement("link.verify").info());
                });

        return true;
    }
}
