package com.github.riku32.discordlink.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.github.riku32.discordlink.Constants;
import com.github.riku32.discordlink.DiscordLink;
import com.github.riku32.discordlink.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@CommandAlias("link")
@Description("Link your account to your discord")
public class LinkCommand extends BaseCommand {
    @Dependency
    private DiscordLink plugin;

    @Default
    private void link(Player player, String tag) throws SQLException {
        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(player.getUniqueId());
        if (playerInfoOptional.isPresent()) {
            PlayerInfo playerInfo = playerInfoOptional.get();
            if (playerInfo.isVerified()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&cYou can't change your linked account after you have completed the link process"));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("&7Currently in the process of linking to &e%s&7, if this is a mistake type &e/unlink&7 or react to the X on the discord message",
                                plugin.getJda().retrieveUserById(playerInfo.getDiscordID()).complete().getAsTag())));
            }
            return;
        }

        Member member;
        try {
            member = Objects.requireNonNull(plugin.getJda().getGuildById(plugin.getGuildID())).getMemberByTag(tag);
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
                    "&cThat discord account is already linked to another minecraft account"));
            return;
        }

        MessageEmbed embed = new EmbedBuilder()
                .setColor(Constants.Colors.SUCCESS)
                .setTitle("Minecraft verification")
                .setDescription("Minecraft to discord verification initiated. Please press the green button to verify the account link process. If you do not want to link accounts or this was not you, press the red button.")
                .addField("Username", player.getName(), true)
                .addField("UUID", player.getUniqueId().toString(), true)
                .build();

        member.getUser().openPrivateChannel().submit()
                .thenCompose(privateChannel -> privateChannel.sendMessage(embed).submit())
                .whenComplete((message, error) -> {
                    if (error != null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&cYour discord account has DMs disabled. Please enable DMs and try again"));
                        return;
                    }

                    try {
                        plugin.getDatabase().createVerificationMessage(player.getUniqueId(), message.getId());
                        plugin.getDatabase().createPlayer(player.getUniqueId(), member.getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // Queue red and green square
                    message.addReaction("\uD83D\uDFE9").queue();
                    message.addReaction("\uD83D\uDFE5").queue();

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&7Please verify in your discord DMs"));
                });
    }
}
