package com.github.riku32.discordlink.core.bot.listeners;

import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.database.DataException;
import com.github.riku32.discordlink.core.database.managers.PlayerManager;
import com.github.riku32.discordlink.core.database.model.PlayerIdentity;
import com.github.riku32.discordlink.core.database.model.PlayerInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class VerificationListener extends ListenerAdapter {
    private final Bot bot;
    private final PlayerManager playerManager;

    public VerificationListener(Bot bot, PlayerManager playerManager) {
        this.bot = bot;
        this.playerManager = playerManager;
    }

    public void onButtonClick(ButtonClickEvent event) {
        if (event.getChannelType().isGuild()) return;
        if (!event.getComponentId().startsWith("link.")) return;

        bot.getGuild().retrieveMemberById(event.getUser().getId()).queue(member -> {
            Objects.requireNonNull(event.getMessage()).editMessage(new MessageBuilder()
                    .setContent(" ")
                    .setActionRows(ActionRow.of(event.getMessage().getButtons().stream().map(Button::asDisabled).collect(Collectors.toList())
                    )).build()).queue();

            Optional<PlayerInfo> optionalPlayerInfo;
            try {
                optionalPlayerInfo = playerManager.getPlayerInfo(PlayerIdentity.from(event.getUser().getId()));
            } catch (DataException exception) {
                exception.printStackTrace();
                return;
            }

            // If the player didn't exist or was verified then something weird happened
            // Just acknowledge the interaction and ignore since the ActionRows already greyed
            if (optionalPlayerInfo.isEmpty()) {
                event.deferEdit().queue();
                return;
            }

            PlayerInfo playerInfo = optionalPlayerInfo.get();
            if (playerInfo.isVerified()) {
                event.deferEdit().queue();
                return;
            }

            try {
                if (!playerManager.getVerificationMessage(PlayerIdentity.from(playerInfo.getDiscordID())).equals(event.getMessageId()))
                    return;
            } catch (DataException exception) {
                exception.printStackTrace();
                return;
            }


        });
    }
}
