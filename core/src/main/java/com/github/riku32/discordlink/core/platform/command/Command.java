package com.github.riku32.discordlink.core.platform.command;

public interface Command {
    void onCommand(CommandSender sender, String label, String[] args);
}
