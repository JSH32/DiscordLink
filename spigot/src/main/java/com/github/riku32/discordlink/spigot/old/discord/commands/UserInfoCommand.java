package com.github.riku32.discordlink.spigot.old.discord.commands;

import com.freya02.botcommands.slash.GuildSlashEvent;
import com.freya02.botcommands.slash.SlashCommand;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.slash.annotations.Option;
import com.github.riku32.discordlink.spigot.Util;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.spigot.Constants;
import com.github.riku32.discordlink.spigot.DiscordLinkSpigot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class UserInfoCommand extends SlashCommand {
    private final DiscordLinkSpigot plugin;

    public UserInfoCommand(DiscordLinkSpigot plugin) {
        this.plugin = plugin;
    }

    @JdaSlashCommand(
            name = "userinfo",
            description = "Get minecraft info about a linked user"
    )
    public void user(GuildSlashEvent event,
                     @Option(name = "user", description = "User to get info about") User user) throws SQLException, IOException {
        Optional<PlayerInfo> playerInfoOptional = plugin.getDatabase().getPlayerInfo(user.getId());

        // User is not linked to minecraft
        if (playerInfoOptional.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Constants.Colors.FAIL)
                    .setTitle("Not linked")
                    .setDescription(String.format("%s has not linked their Minecraft account", user.getAsMention()))
                    .build()).queue();
            return;
        }

        PlayerInfo playerInfo = playerInfoOptional.get();
        OfflinePlayer bukkitPlayer = Bukkit.getOfflinePlayer(playerInfo.getUuid());

        String lastPlayed;
        if (bukkitPlayer.isOnline())
            lastPlayed = "*Currently online*";
        else
            lastPlayed = String.format("%s ago", formatDuration(Duration.between(
                    new Date(bukkitPlayer.getLastPlayed()).toInstant(),
                    new Date().toInstant())));

        event.reply(new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setColor(Constants.Colors.SUCCESS)
                        .setTitle(String.format("%s", user.getAsTag()))
                        .setThumbnail("attachment://head.png")
                        .addField("Username", bukkitPlayer.getName(), true)
                        .addField("Last played", lastPlayed, true)
                        .addField("UUID", playerInfo.getUuid().toString(), false)
                        .build())
                .build())
                .addFile(Util.getIsometricHeadStream(bukkitPlayer.getUniqueId()), "head.png")
                .queue();
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();

        ArrayList<String> itemList = new ArrayList<>();

        if (days != 0) itemList.add(days + " days");
        if (hours != 0) itemList.add(hours + " hours");
        itemList.add(minutes + " minutes");

        return String.join(", ", itemList);
    }
}
