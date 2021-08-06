package com.github.riku32.discordlink.spigot.old.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.github.riku32.discordlink.spigot.DiscordLinkSpigot;
import com.github.riku32.discordlink.spigot.Util;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.spigot.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Optional;

@CommandAlias("link")
@Description("Link your account to your discord")
public class LinkCommand extends BaseCommand {
    @Dependency
    private DiscordLinkSpigot plugin;

    @Default
    private void link(Player player, String tag) throws SQLException, IOException {
        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(player.getUniqueId());
        if (playerInfoOptional.isPresent()) {
            PlayerInfo playerInfo = playerInfoOptional.get();

            plugin.getBot().getJda().retrieveUserById(playerInfo.getDiscordID()).queue(user -> {
                if (playerInfo.isVerified()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            String.format("&cYour account is already linked to &e%s. %s", user.getAsTag(),
                                    plugin.getPluginConfig().isAllowUnlink() ? "&cIf you want to unlink your account type &e/unlink" : "")));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            String.format("&7Currently in the process of linking to &e%s&7, if this is a mistake type &e/cancel&7 or click cancel on the discord message",
                                    user.getAsTag())));
                }
            });

            return;
        }

        Member member;
        try {
            member = plugin.getBot().getGuild().getMemberByTag(tag);
        } catch (IllegalArgumentException ignored) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cInvalid tag provided, must be in the discord server to link"));
            return;
        }

        if (member == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cInvalid tag provided, must be in the discord server to link"));
            return;
        }

        if (plugin.getDatabase().isDiscordLinked(member.getId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cThat discord account is already linked or in the process of linking with another minecraft account"));
            return;
        }

        Message verificationMessage = new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                    .setColor(Constants.Colors.SUCCESS)
                    .setTitle("Minecraft Link")
                    .setThumbnail("attachment://head.png")
                    .setDescription("Minecraft to Discord link initiated. Press verify to complete the account link process. If you do not want to link accounts or this was not you, press cancel." +
                            (!plugin.getPluginConfig().isAllowUnlink() ? "\n\n⚠ **THIS CANNOT BE UNDONE**" : ""))
                    .addField("Username", player.getName(), true)
                    .addField("UUID", player.getUniqueId().toString(), true)
                    .build())
                .setActionRows(ActionRow.of(
                        Button.success("link.verify", "Verify"),
                        Button.danger("link.cancel", "Cancel")
                        ))
                .build();

        InputStream head = Util.getIsometricHeadStream(player.getUniqueId());

        member.getUser().openPrivateChannel().submit()
                .thenCompose(privateChannel -> privateChannel.sendMessage(verificationMessage)
                        .addFile(head, "head.png")
                        .submit())
                .whenComplete((message, error) -> {
                    if (error != null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&cYour discord account has DMs disabled. Please enable DMs and try again"));
                        return;
                    }

                    try {
                        plugin.getDatabase().createPlayer(player.getUniqueId(), member.getId(), message.getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&7Please verify in your discord DMs. Type &e/cancel&7 to cancel this process"));
                });
    }
}
