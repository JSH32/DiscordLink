package com.github.riku32.discordlink.spigot.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.spigot.Constants;
import com.github.riku32.discordlink.spigot.DiscordLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandAlias("cancel")
@Description("Cancel the discord linking process")
public class CancelCommand extends BaseCommand {
    @Dependency
    private DiscordLink plugin;

    @Default
    private void cancel(Player player) throws SQLException {
        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(player.getUniqueId());
        if (playerInfoOptional.isEmpty() || playerInfoOptional.get().isVerified()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cYour account is not currently linking to a discord account"));
            return;
        }

        PlayerInfo playerInfo = playerInfoOptional.get();

        String messageID = plugin.getDatabase().getMessageId(playerInfo.getDiscordID());
        if (messageID != null) {
            plugin.getBot().getJda().openPrivateChannelById(playerInfo.getDiscordID())
                    .queue(channel -> channel.retrieveMessageById(messageID).queue(message -> message.editMessage(new MessageBuilder()
                            .setContent(" ")
                            .setActionRows(ActionRow.of(message.getButtons().stream().map(Button::asDisabled).collect(Collectors.toList())))
                            .build())
                            .queue()));
        }

        plugin.getDatabase().deletePlayer(player.getUniqueId());

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&7You have cancelled the linking process"));

        plugin.getBot().getGuild().retrieveMemberById(playerInfo.getDiscordID())
                .flatMap(member -> member.getUser().openPrivateChannel())
                .flatMap(channel -> channel.sendMessage(new EmbedBuilder()
                        .setTitle("Cancelled")
                        .setDescription(String.format("%s has cancelled the linking process", player.getName()))
                        .setColor(Constants.Colors.FAIL)
                        .build()))
                .queue();
    }
}
