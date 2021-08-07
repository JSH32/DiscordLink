package com.github.riku32.discordlink.core.commands;

import com.github.riku32.discordlink.core.platform.command.CommandSender;
import com.github.riku32.discordlink.core.platform.command.annotation.Choice;
import com.github.riku32.discordlink.core.platform.command.annotation.Command;
import com.github.riku32.discordlink.core.platform.command.annotation.Default;

@Command(
        aliases = {"test"},
        permission = "discordlink.test"
)
public class CommandTest {
    @Default
    public void test(CommandSender sender) {
        sender.sendMessage("testing");
    }

    @Command(aliases = {"sub"})
    public void sub(CommandSender sender, @Choice({"yes", "no"}) String choice) {
        sender.sendMessage("You said " + choice);
    }
}
