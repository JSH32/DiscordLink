package com.github.riku32.discordlink.core.commands;

import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.platform.command.CommandSender;
import com.github.riku32.discordlink.core.platform.command.annotation.Choice;
import com.github.riku32.discordlink.core.platform.command.annotation.Command;
import com.github.riku32.discordlink.core.platform.command.annotation.Default;

@Command(aliases = {"link"})
public class CommandLink {
    private final DiscordLink plugin;

    public CommandLink(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @Default
    private boolean link(CommandSender sender) {
        sender.sendMessage(plugin.getLocale().getElement("link.example").set("player", sender.getName()).info());
        return true;
    }

    @Command(
            aliases = {"subcommand"},
            userOnly = true
    )
    private boolean subcommand(CommandSender sender, @Choice({"coffee", "tea"}) String drink) {
        sender.sendMessage(plugin.getLocale().getElement("link.example").set("player", sender.getName()).success());
        sender.sendMessage("You like " + drink);
        return true;
    }
}
