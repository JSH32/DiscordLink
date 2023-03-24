package com.github.jsh32.discordlink.core.commands;

import com.github.jsh32.discordlink.core.bot.Bot;
import com.github.jsh32.discordlink.core.database.PlayerInfo;
import com.github.jsh32.discordlink.core.database.Verification;
import com.github.jsh32.discordlink.core.framework.PlatformPlugin;
import com.github.jsh32.discordlink.core.framework.command.CommandSender;
import com.github.jsh32.discordlink.core.framework.command.annotation.Command;
import com.github.jsh32.discordlink.core.framework.command.annotation.Default;
import com.github.jsh32.discordlink.core.framework.dependency.annotation.Dependency;
import com.github.jsh32.discordlink.core.locale.Locale;
import io.ebean.DB;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.Optional;
import java.util.stream.Collectors;

@Command(
        aliases = {"cancel"},
        permission = "discordlink.link",
        userOnly = true
)
public class CommandCancel {
    @Dependency
    private Bot bot;

    @Dependency
    private Locale locale;

    @Dependency
    private PlatformPlugin plugin;

    @Default
    private void cancel(CommandSender sender) {
        Optional<PlayerInfo> playerInfoOptional = PlayerInfo.find.byUuidOptional(sender.getPlayer().getUuid());

        if (playerInfoOptional.isEmpty() || playerInfoOptional.get().verified) {
            sender.sendMessage(locale.getElement("cancel.not_linking").error());
        } else {
            PlayerInfo playerInfo = playerInfoOptional.get();
            Optional<Verification> verification = Verification.find.byMember(playerInfo.discordId);
            DB.delete(playerInfo);

            verification.ifPresent(value -> bot.getJda().openPrivateChannelById(playerInfo.discordId)
                    .queue(channel -> channel.retrieveMessageById(value.verificationValue).queue(message ->
                            message.editMessage(new MessageBuilder()
                                    .setContent(" ")
                                    .setActionRows(ActionRow.of(message.getButtons().stream().map(Button::asDisabled).collect(Collectors.toList())))
                                    .build())
                                    .queue())));

            sender.sendMessage(locale.getElement("cancel.success").success());
        }
    }
}
