package com.github.riku32.discordlink.core.commands;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.core.platform.command.CommandSender;
import com.github.riku32.discordlink.core.platform.command.annotation.Choice;
import com.github.riku32.discordlink.core.platform.command.annotation.Command;
import com.github.riku32.discordlink.core.platform.command.annotation.Default;

@Command(aliases = {"test"})
public class CommandTest {
    @Default
    public void test(CommandSender sender, @Choice({"hi"}) String testString, boolean testBool, PlatformPlayer player) {
        sender.sendMessage(testString);
        sender.sendMessage(String.valueOf(testBool));
        sender.sendMessage(player.getName());
    }

    @Command(aliases = {"sub"})
    public void sub(CommandSender sender, @Choice({"yes", "no"}) String choice) {
        sender.sendMessage("You said " + choice);
    }
}
