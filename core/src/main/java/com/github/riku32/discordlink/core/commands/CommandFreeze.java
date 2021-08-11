package com.github.riku32.discordlink.core.commands;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.core.platform.command.CommandSender;
import com.github.riku32.discordlink.core.platform.command.annotation.Command;
import com.github.riku32.discordlink.core.platform.command.annotation.Default;

import java.util.Set;

// Test command, delete this
@Command(
        aliases = {"freeze"},
        userOnly = true
)
public class CommandFreeze {
    private final Set<PlatformPlayer> frozenPlayers;

    public CommandFreeze(Set<PlatformPlayer> frozenPlayers) {
        this.frozenPlayers = frozenPlayers;
    }

    @Default
    private boolean freeze(CommandSender sender, PlatformPlayer player, boolean freezeStatus) {
        if (freezeStatus) {
            if (!frozenPlayers.add(player)) {
                sender.sendMessage("Player already frozen");
                return false;
            }

            sender.sendMessage("Player frozen");
        } else {
            if (!frozenPlayers.remove(player)) {
                sender.sendMessage("Player not frozen");
                return false;
            }

            sender.sendMessage("Player unfrozen");
        }

        return true;
    }
}
