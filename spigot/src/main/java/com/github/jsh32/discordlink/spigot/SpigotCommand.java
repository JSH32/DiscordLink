package com.github.jsh32.discordlink.spigot;

import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.command.CommandData;
import com.github.jsh32.discordlink.core.framework.command.CommandSender;
import com.github.jsh32.discordlink.core.framework.command.CompiledCommand;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.ExecutorType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class SpigotCommand {
    private final DiscordLinkSpigot plugin;
    private final PlayerRegistry playerRegistry;

    public SpigotCommand(DiscordLinkSpigot plugin, PlayerRegistry playerRegistry) {
        this.plugin = plugin;
        this.playerRegistry = playerRegistry;

        CommandAPI.onLoad(new CommandAPIConfig().silentLogs(true));
        CommandAPI.onEnable(plugin);
    }

    public void onDisable() {
        CommandAPI.unregister("dl");
        CommandAPI.onDisable();
    }

    private Argument<Member> memberArgument(String nodeName) {
        // Construct our CustomArgument that takes in a String input and returns a World object
        return new CustomArgument<>(new TextArgument(nodeName), (input) -> {
            String parsed = input.currentInput();
            int lastIndex = parsed.lastIndexOf(".");
            if (lastIndex == -1) {
                throw new CustomArgument.CustomArgumentException(
                        new CustomArgument.MessageBuilder("Invalid tag: ").appendArgInput());
            }

            try {
                return Objects.requireNonNull(plugin.getDiscordLink().getBot().getGuild().getMemberByTag(
                        parsed.substring(0, lastIndex),
                        parsed.substring(lastIndex + 1)
                ));
            } catch (Exception ignored) {
                throw new CustomArgument.CustomArgumentException(
                        new CustomArgument.MessageBuilder("Invalid member: ").appendArgInput());
            }
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
            plugin.getDiscordLink().getBot().getGuild().getMemberCache().stream().filter(u -> {
                User user = u.getUser();
                String searchString = user.getName() + "." + user.getDiscriminator();
                return searchString.startsWith(info.currentArg());
            }).map(u -> {
                User user = u.getUser();
                String searchString = user.getName() + "." + user.getDiscriminator();
                return searchString.contains(" ") ? "\"" + searchString + "\"" : searchString;
            }).toArray(String[]::new)
        ));
    }

    private CommandAPICommand createCommand(CommandData command, boolean explicitPlayerOnly) {
        String[] aliases = command.getAliases();

        List<Argument<?>> args = command.getArguments().stream().map(arg -> {
            if (arg.argumentType().equals(boolean.class)) {
                return new BooleanArgument(arg.argumentName());
            } else if (arg.argumentType().equals(String.class)) {
                if (arg.choices() == null)
                    return new TextArgument(arg.argumentName());

                return new MultiLiteralArgument(arg.choices());
            } else if (arg.argumentType().equals(PlatformPlayer.class)) {
                return new PlayerArgument(arg.argumentName());
            } else if (arg.argumentType().equals(Member.class)) {
                return memberArgument(arg.argumentName());
            }

            // This should never happen due to the command compiler checks.
            throw new IllegalArgumentException("Invalid argument type");
        }).collect(Collectors.toList());

        return new CommandAPICommand(aliases[0])
                .withPermission(CommandPermission.fromString(command.getPermission()))
                .withArguments(args)
                .withAliases(Arrays.copyOfRange(command.getAliases(), 1, aliases.length))
                .executes((sender, cmdArgs) -> {
                    CommandSender commandSender = new CommandSender(
                            sender instanceof Player ? playerRegistry.getPlayer((Player) sender) : null, plugin);

                    List<Object> fullArgs = new ArrayList<>();
                    fullArgs.add(commandSender);

                    for (Object cmdArg : cmdArgs) {
                        if (cmdArg instanceof Player) {
                            fullArgs.add(playerRegistry.getPlayer((Player) cmdArg));
                        } else {
                            fullArgs.add(cmdArg);
                        }
                    }

                    try {
                        command.invoke(fullArgs.toArray());
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }, explicitPlayerOnly || command.isUserOnly()
                        ? new ExecutorType[]{ ExecutorType.PLAYER }
                        : new ExecutorType[]{ ExecutorType.PLAYER, ExecutorType.CONSOLE });
    }

    public void registerCommands(List<CompiledCommand> commands) {
        CommandAPICommand apiCommand = new CommandAPICommand("dl")
                .withAliases("discordlink")
                .withPermission(CommandPermission.fromString("discord.use"))
                .withShortDescription(plugin.getDiscordLink().getLocale().getElement("command.description").toString())
                .executes((sender, args) -> {
                    sender.sendMessage(plugin.getDiscordLink().getLocale().getElement("command.version")
                            .set("version", plugin.getDescription().getVersion()).component(true));
                });

        apiCommand.setSubcommands(commands.stream().map(compiledCommand -> {
            var cmd = createCommand(compiledCommand.getBaseCommand(), compiledCommand.isUserOnly());
            cmd.setSubcommands(compiledCommand.getSubCommands().stream().map(c -> createCommand(c, false)).collect(Collectors.toList()));
            return cmd;
        }).collect(Collectors.toList()));

        apiCommand.register();
    }
}
