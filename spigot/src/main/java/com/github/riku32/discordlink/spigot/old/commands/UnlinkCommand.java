package com.github.riku32.discordlink.spigot.old.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.spigot.Constants;
import com.github.riku32.discordlink.spigot.DiscordLinkSpigot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;

@CommandAlias("unlink")
@Description("Unlink your minecraft account with your discord account")
public class UnlinkCommand extends BaseCommand {
    @Dependency
    private DiscordLinkSpigot plugin;
//
//    @Default
//    private void unlink(Player player) throws SQLException {
//        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(player.getUniqueId());
//        if (playerInfoOptional.isEmpty()) {
//            player.sendMessage(ChatColor.RED + "Your account is not currently linked to a discord account.");
//            return;
//        }
//
//        PlayerInfo playerInfo = playerInfoOptional.get();
//        if (!playerInfo.isVerified()) {
//            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
//                    "&cYour account is in the process of linking but not quite linked. Please use &e/cancel&c instead."));
//            return;
//        }
//
//        plugin.getDatabase().deletePlayer(player.getUniqueId());
//        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
//                "&7You have unlinked your accounts"));
//
//        plugin.getBot().getGuild().retrieveMemberById(playerInfo.getDiscordID())
//                .flatMap(member -> member.getUser().openPrivateChannel())
//                .flatMap(channel -> channel.sendMessage(new EmbedBuilder()
//                        .setTitle("Unlinked")
//                        .setDescription(String.format("%s has unlinked their minecraft account", player.getName()))
//                        .setColor(Constants.Colors.FAIL)
//                        .build()))
//                .queue();
//
//        if (plugin.getPluginConfig().isLinkRequired()) plugin.getFrozenPlayers().add(player.getUniqueId());
//    }
}
