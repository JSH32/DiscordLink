package com.github.riku32.discordlink.core.commands;

public interface Command {
    boolean onCommand(CommandSender sender, String label, String[] args);
}
