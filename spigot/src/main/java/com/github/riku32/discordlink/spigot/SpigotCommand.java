package com.github.riku32.discordlink.spigot;

import com.github.riku32.discordlink.core.platform.command.ArgumentData;
import com.github.riku32.discordlink.core.platform.command.CommandData;
import com.github.riku32.discordlink.core.platform.command.CompiledCommand;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpigotCommand extends Command {
    private final CompiledCommand compiledCommand;
    private final DiscordLinkSpigot plugin;

    public SpigotCommand(String name, CompiledCommand compiledCommand, DiscordLinkSpigot plugin) {
        super(name);
        this.compiledCommand = compiledCommand;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        com.github.riku32.discordlink.core.platform.command.CommandSender commandSender =
                new com.github.riku32.discordlink.core.platform.command.CommandSender(sender instanceof Player ? new SpigotPlayer((Player) sender) : null, plugin);

        if (args.length < 1) {
            if (compiledCommand.getBaseCommand().getArgumentData().size() != 0) {
                commandSender.sendMessage("Invalid arguments provided");
                return false;
            }

            try {
                compiledCommand.getBaseCommand().getMethod().invoke(compiledCommand.getBaseCommand().getInstance(), commandSender);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        for (CommandData commandData : compiledCommand.getSubCommands()) {
            for (String alias : commandData.getAliases()) {
                if (alias.equals(args[0])) {
                    try {
                        // Subtract one from length since one of the args will be the subcommand name
                        if (commandData.getArgumentData().size() != args.length - 1) {
                            commandSender.sendMessage("Invalid arguments provided");
                            return false;
                        }

                        for (int i = 0; i < commandData.getArgumentData().size(); i++) {
                            ArgumentData argumentData = commandData.getArgumentData().get(i);
                            if (argumentData.getChoices() == null) continue;

                            if (!Arrays.asList(argumentData.getChoices()).contains(args[i + 1])) {
                                commandSender.sendMessage("Invalid arguments provided");
                                return false;
                            }
                        }

                        Object[] arguments = new Object[args.length];
                        arguments[0] = commandSender;
                        System.arraycopy(args, 1, arguments, 1, args.length - 1);

                        commandData.getMethod().invoke(compiledCommand.getBaseCommand().getInstance(), arguments);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
        }

        if (compiledCommand.getBaseCommand().getArgumentData().size() != args.length) {
            commandSender.sendMessage("Invalid arguments provided");
            return false;
        }

        for (int i = 0; i < compiledCommand.getBaseCommand().getArgumentData().size(); i++) {
            ArgumentData argumentData = compiledCommand.getBaseCommand().getArgumentData().get(i);
            if (argumentData.getChoices() == null) continue;

            if (!Arrays.asList(argumentData.getChoices()).contains(args[i])) {
                commandSender.sendMessage("Invalid arguments provided");
                return false;
            }
        }

        Object[] arguments = new Object[args.length + 1];
        arguments[0] = commandSender;
        System.arraycopy(args, 0, arguments, 1, args.length);

        try {
            compiledCommand.getBaseCommand().getMethod().invoke(compiledCommand.getBaseCommand().getInstance(), arguments);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length < 1) return ImmutableList.of();

        // Returns for subcommands and choices for base command
        if (args.length == 1) {
            List<String> argList = new ArrayList<>();
            for (CommandData commandData : compiledCommand.getSubCommands())
                argList.addAll(Arrays.asList(commandData.getAliases()));

            if (compiledCommand.getBaseCommand().getArgumentData().size() > 0) {
                String[] choices = compiledCommand.getBaseCommand().getArgumentData().get(0).getChoices();
                if (choices != null)
                    argList.addAll(Arrays.asList(choices));
            }

            return argList;
        }

        // Completion for subcommands at any stage
        for (CommandData commandData : compiledCommand.getSubCommands()) {
            if (!Arrays.asList(commandData.getAliases()).contains(args[0])) continue;

            List<String> argList = new ArrayList<>();

            // Subtract by two because subcommand and accessing list
            if (commandData.getArgumentData().size() > args.length - 2) {
                String[] choices = commandData.getArgumentData().get(args.length - 2).getChoices();
                if (choices != null)
                    argList.addAll(Arrays.asList(choices));
            }

            return argList;
        }

        // Completion for base at any stage
        List<String> argList = new ArrayList<>();
        if (compiledCommand.getBaseCommand().getArgumentData().size() > args.length - 1) {
            String[] choices = compiledCommand.getBaseCommand().getArgumentData().get(args.length - 1).getChoices();
            if (choices != null)
                argList.addAll(Arrays.asList(choices));
        }

        return argList;
    }
}
