package com.github.riku32.discordlink.Listeners;

import com.github.riku32.discordlink.Constants;
import com.github.riku32.discordlink.DiscordLink;
import com.github.riku32.discordlink.PlayerInfo;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class VerificationListener extends ListenerAdapter {
    private final DiscordLink plugin;

    public VerificationListener(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @Override
    @SneakyThrows
    public void onButtonClick(ButtonClickEvent e) {
        if (e.getChannelType().isGuild()) return;

        if (e.getComponentId().equals("verify_link") || e.getComponentId().equals("cancel_link")) {
            if (!plugin.getDatabase().isVerificationMessage(e.getMessageId()))
                return;

            Objects.requireNonNull(e.getMessage()).editMessage(new MessageBuilder()
                .setContent(" ")
                .setActionRows(ActionRow.of(e.getMessage().getButtons().stream().map(Button::asDisabled).collect(Collectors.toList())
                )).build()).queue();

            Optional<PlayerInfo> optionalPlayerInfo = plugin.getDatabase().getPlayerInfo(e.getUser().getId());
            if (!optionalPlayerInfo.isPresent()) return;
            PlayerInfo playerInfo = optionalPlayerInfo.get();

            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerInfo.getUuid());

            switch (e.getComponentId()) {
                case "verify_link": {
                    e.replyEmbeds(new EmbedBuilder()
                            .setTitle("Linked")
                            .setDescription(String.format("Your discord account has been linked to %s", offlinePlayer.getName()))
                            .setColor(Constants.Colors.SUCCESS)
                            .build()).queue();

                    plugin.getDatabase().deleteVerificationMessage(e.getMessageId());
                    plugin.getDatabase().verifyPlayer(e.getUser().getId());

                    if (offlinePlayer.isOnline()) {
                        Player player = Objects.requireNonNull(offlinePlayer.getPlayer());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                String.format("&7Your minecraft account has been linked to &e%s", e.getUser().getAsTag())));

                        plugin.getFrozenPlayers().remove(player.getUniqueId());

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.teleport(player.getWorld().getSpawnLocation());
                            player.setGameMode(plugin.getServer().getDefaultGameMode());
                        });
                    }

                    break;
                }
                case "cancel_link": {
                    e.replyEmbeds(new EmbedBuilder()
                            .setTitle("Cancelled")
                            .setDescription("You have cancelled the linking process")
                            .setColor(Constants.Colors.FAIL)
                            .build()).queue();

                    if (offlinePlayer.isOnline()) {
                        Objects.requireNonNull(offlinePlayer.getPlayer()).sendMessage(ChatColor.translateAlternateColorCodes('&',
                                String.format("&e%s&7 has cancelled the linking process", e.getUser().getAsTag())));
                    }

                    plugin.getDatabase().deleteVerificationMessage(e.getMessageId());
                    plugin.getDatabase().deletePlayer(e.getUser().getId());
                    break;
                }
            }
        }
    }
}
