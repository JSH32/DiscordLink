package com.github.riku32.discordlink.spigot;

import com.github.riku32.discordlink.core.locale.Locale;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.framework.command.ArgumentData;
import com.github.riku32.discordlink.core.framework.command.CommandData;
import com.github.riku32.discordlink.core.framework.command.CompiledCommand;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

// TODO: Clean this entire class up
public class SpigotCommand implements CommandExecutor, TabCompleter {
    private final Map<String, CompiledCommand> commandMap = new HashMap<>();
    private final DiscordLinkSpigot plugin;
    private final PlayerRegistry playerRegistry;
    private Locale locale;

    public SpigotCommand(DiscordLinkSpigot plugin, PlayerRegistry playerRegistry) {
        this.plugin = plugin;
        this.playerRegistry = playerRegistry;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void addCommand(CompiledCommand compiledCommand) {
        Arrays.stream(compiledCommand.getBaseCommand().getAliases())
                .forEach(alias -> commandMap.put(alias, compiledCommand));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command commandObj, @NotNull String label, @NotNull String[] args) {
        Player bukkitPlayer = null;

        if (commandSender instanceof Player)
            bukkitPlayer = (Player) commandSender;

        com.github.riku32.discordlink.core.framework.command.CommandSender sender =
                new com.github.riku32.discordlink.core.framework.command.CommandSender(bukkitPlayer != null ? playerRegistry.getPlayer(bukkitPlayer) : null, plugin);

        if (args.length < 1) {
            sender.sendMessage(locale.getElement("command.version")
                    .set("version", plugin.getDescription().getVersion()).component(true));
            return true;
        }

        CompiledCommand command = commandMap.get(args[0]);
        if (command == null) {
            sender.sendMessage(locale.getElement("command.invalid_command").error());
            return false;
        }

        if (command.getBaseCommand().isUserOnly() && sender.isConsole()) {
            sender.sendMessage(locale.getElement("command.no_console")
                    .set("command", args[0]).error());
            return false;
        }

        // Make sure base command permission is met
        if (command.getBaseCommand().getPermission() != null && !commandSender.hasPermission(command.getBaseCommand().getPermission())) {
            sender.sendMessage(locale.getElement("command.no_permission")
                    .set("permission", command.getBaseCommand().getPermission()).error());
            return false;
        }

        // Execute base command without arguments
        if (args.length == 1) {
            if (command.getBaseCommand().getArguments().size() != 0) {
                sender.sendMessage(locale.getElement("command.invalid_args")
                        .set("command", args[0]).error());
                return false;
            }

            try {
                return (boolean) command.getBaseCommand().getMethod().invoke(command.getBaseCommand().getInstance(), sender);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // Execute sub commands with arguments
        for (CommandData commandData : command.getSubCommands()) {
            for (String alias : commandData.getAliases()) {
                // Arg 0 will contain the base command name since the command is prefixed, Arg 1 will contain the subcommand
                if (alias.equals(args[1])) {
                    try {
                        if (commandData.isUserOnly() && sender.isConsole()) {
                            sender.sendMessage(locale.getElement("command.no_console")
                                    .set("command", String.format("%s %s", args[0], alias)).error());
                            return false;
                        }

                        // Check if sub command permissions are met
                        if (commandData.getPermission() != null && !commandSender.hasPermission(commandData.getPermission())) {
                            sender.sendMessage(locale.getElement("command.no_permission")
                                    .set("permission", commandData.getPermission()).error());
                            return false;
                        }

                        // Subtract two from length since one of the args will be the subcommand name and one will be base command
                        if (commandData.getArguments().size() != args.length - 2) {
                            sender.sendMessage(locale.getElement("command.invalid_args")
                                    .set("command", String.format("%s %s", args[0], alias)).error());
                            return false;
                        }

                        Object[] arguments = new Object[args.length - 1];
                        arguments[0] = sender;
                        System.arraycopy(args, 2, arguments, 1, args.length - 2);

                        for (int i = 2; i < args.length; i++) {
                            ArgumentParseResult parseResult = parseArgument(commandData.getArguments().get(i - 2), args[i]);

                            if (!parseResult.success) {
                                sender.sendMessage(locale.getElement("command.invalid_args")
                                        .set("command", String.format("%s %s", args[0], alias)).error());
                                return false;
                            }

                            arguments[i - 1] = parseResult.parsed;
                        }

                        return (boolean) commandData.getMethod().invoke(command.getBaseCommand().getInstance(), arguments);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }

        // Validate arguments for base with args, subtract one from length because command name is an arg
        if (command.getBaseCommand().getArguments().size() != args.length - 1) {
            sender.sendMessage(locale.getElement("command.invalid_args")
                    .set("command", args[0]).error());
            return false;
        }

        Object[] arguments = new Object[args.length];
        arguments[0] = sender;
        System.arraycopy(args, 1, arguments, 1, args.length - 1);

        for (int i = 1; i < args.length; i++) {
            ArgumentParseResult parseResult = parseArgument(command.getBaseCommand().getArguments().get(i - 1), args[i]);

            if (!parseResult.success) {
                sender.sendMessage(locale.getElement("command.invalid_args")
                        .set("command", args[0]).error());
                return false;
            }

            // Set the argument to one after
            arguments[i] = parseResult.parsed;
        }

        try {
            return (boolean) command.getBaseCommand().getMethod().invoke(command.getBaseCommand().getInstance(), arguments);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length < 1) return ImmutableList.of("");

        // Returns for base command name
        if (args.length == 1)
            return new ArrayList<>(commandMap.keySet());

        CompiledCommand compiledCommand = commandMap.get(args[0]);
        if (compiledCommand == null) return ImmutableList.of("");

        // Returns for subcommands and choices for base command
        if (args.length == 2) {
            if (compiledCommand.getBaseCommand().getArguments().size() > 0)
                return getCompletion(compiledCommand.getBaseCommand().getArguments().get(0), compiledCommand.getSubCommands());

            return getCompletion(null, compiledCommand.getSubCommands());
        }

        // Completion for subcommands at any stage
        for (CommandData commandData : compiledCommand.getSubCommands()) {
            if (!Arrays.asList(commandData.getAliases()).contains(args[1])) continue;
            if (commandData.getArguments().size() > args.length - 3)
                return getCompletion(commandData.getArguments().get(args.length - 3), null);

            return ImmutableList.of("");
        }

        // Completion for base at any stage
        if (compiledCommand.getBaseCommand().getArguments().size() > args.length - 2)
            return getCompletion(compiledCommand.getBaseCommand().getArguments().get(args.length - 2), null);

        return ImmutableList.of("");
    }

    private static class ArgumentParseResult {
        public final boolean success;
        public final Object parsed;

        public ArgumentParseResult(boolean success, Object parsed) {
            this.success = success;
            this.parsed = parsed;
        }
    }

    private ArgumentParseResult parseArgument(ArgumentData argumentData, String argument) {
        if (argumentData == null)
            return new ArgumentParseResult(false, null);

        if (argumentData.getArgumentType() == boolean.class) {
            if (!argument.equals("true") && !argument.equals("false"))
                return new ArgumentParseResult(false, null);

            return new ArgumentParseResult(true, Boolean.parseBoolean(argument));
        } else if (argumentData.getArgumentType() == String.class) {
            if (argumentData.getChoices() == null)
                return new ArgumentParseResult(true, argument);

            if (Arrays.asList(argumentData.getChoices()).contains(argument))
                return new ArgumentParseResult(true, argument);
        } else if (argumentData.getArgumentType() == PlatformPlayer.class) {
            PlatformPlayer player = plugin.getPlayer(argument);
            if (player != null)
                return new ArgumentParseResult(true, player);
        }

        return new ArgumentParseResult(false, null);
    }

    private List<String> getCompletion(ArgumentData argument, Set<CommandData> subCommands) {
        List<String> completions = new ArrayList<>();

        if (argument != null) {
            if (argument.getArgumentType() == boolean.class) {
                completions.addAll(ImmutableList.of("true", "false"));
            } else if (argument.getArgumentType() == String.class) {
                if (argument.getChoices() != null)
                    completions.addAll(Arrays.asList(argument.getChoices()));
            } else if (argument.getArgumentType() == PlatformPlayer.class) {
                completions.addAll(plugin.getPlayers().stream()
                        .map(PlatformPlayer::getName)
                        .collect(Collectors.toUnmodifiableSet()));
            }
        }

        if (subCommands != null) {
            completions.addAll(subCommands.stream()
                    .map(CommandData::getAliases)
                    .map(Arrays::asList)
                    .flatMap(List::stream)
                    .collect(Collectors.toUnmodifiableList()));
        }

        return completions;
    }
}
