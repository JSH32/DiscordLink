package com.github.jsh32.discordlink.spigot;

import com.github.jsh32.discordlink.core.DiscordLink;
import com.github.jsh32.discordlink.core.framework.GameMode;
import com.github.jsh32.discordlink.core.framework.PlatformOfflinePlayer;
import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.PlatformPlugin;
import com.github.jsh32.discordlink.core.framework.command.CommandData;
import com.github.jsh32.discordlink.core.framework.command.CommandSender;
import com.github.jsh32.discordlink.core.framework.command.CompiledCommand;
import com.github.jsh32.discordlink.core.framework.eventbus.EventBus;
import com.github.jsh32.discordlink.spigot.events.MainListener;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DiscordLinkSpigot extends JavaPlugin implements PlatformPlugin {
    public static DiscordLinkSpigot INSTANCE;

    private DiscordLink discordLink;
    private EventBus eventBus;
//    private SpigotCommand commandManager;
    private PlayerRegistry playerRegistry;

    @Override
    public void onEnable() {
        CommandAPI.onLoad(new CommandAPIConfig().silentLogs(true));
        CommandAPI.onEnable(this);

        INSTANCE = this;

        this.playerRegistry = new PlayerRegistry();
        getServer().getPluginManager().registerEvents(playerRegistry, this);

        this.eventBus = new EventBus();
        getServer().getPluginManager().registerEvents(new MainListener(eventBus, playerRegistry), this);

//        this.commandManager = new SpigotCommand(this, playerRegistry);

        // This should automatically create and register the platform plugin
        discordLink = new DiscordLink(this);
//        commandManager.setLocale(discordLink.getLocale());

        // Register command after initialization
//        PluginCommand mainCommand = Objects.requireNonNull(this.getCommand("discord"));
//        mainCommand.setExecutor(commandManager);
//        mainCommand.setTabCompleter(commandManager);
    }

    @Override
    public void onDisable() { discordLink.disable(false); }

    @Override
    public PlatformPlayer getPlayer(UUID uuid) {
        Player player = this.getServer().getPlayer(uuid);
        return player == null ? null : playerRegistry.getPlayer(player);
    }

    @Override
    public PlatformOfflinePlayer getOfflinePlayer(UUID uuid) {
        return new SpigotOfflinePlayer(this.getServer().getOfflinePlayer(uuid));
    }

    @Override
    public PlatformPlayer getPlayer(String username) {
        Player player = this.getServer().getPlayer(username);
        return player == null ? null : playerRegistry.getPlayer(player);
    }

    @Override
    public Set<PlatformPlayer> getPlayers() {
        return getServer().getOnlinePlayers().stream().map(playerRegistry::getPlayer)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @NotNull Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public File getDataDirectory() {
        return getDataFolder();
    }

    @Override
    public void disable() {
        if (discordLink != null) discordLink.disable(false);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    private CommandAPICommand createCommand(CommandData command) {
        String[] aliases = command.getAliases();

        var args = command.getArguments().stream().map(arg -> {
            if (arg.getArgumentType().equals(boolean.class)) {
                return new BooleanArgument(arg.getArgumentName());
            } else if (arg.getArgumentType().equals(String.class)) {
                if (arg.getChoices() == null)
                    return new TextArgument(arg.getArgumentName());

                return new MultiLiteralArgument(arg.getChoices());
            } else if (arg.getArgumentType().equals(PlatformPlayer.class)) {
                return new PlayerArgument(arg.getArgumentName());
            }

            return new StringArgument("");
        }).collect(Collectors.toList());

        return new CommandAPICommand(aliases[0])
                .withPermission(command.getAnnotation().permission())
                .withArguments(args)
                .withAliases(Arrays.copyOfRange(command.getAliases(), 1, aliases.length))
                .executes((sender, cmdArgs) -> {
                    CommandSender commandSender = new CommandSender(
                            sender instanceof Player ? playerRegistry.getPlayer((Player) sender) : null, this);

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
                        command.getMethod().invoke(command.getInstance(), fullArgs.toArray());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void registerCommands(List<CompiledCommand> commands) {
        CommandAPICommand apiCommand = new CommandAPICommand("dl")
                .withAliases("discordlink")
                .withPermission(CommandPermission.fromString("discord.use"))
                .executes((sender, args) -> {
                    sender.sendMessage(discordLink.getLocale().getElement("command.version")
                            .set("version", this.getDescription().getVersion()).component(true));
                });

        apiCommand.setSubcommands(commands.stream().map(compiledCommand -> {
            var cmd = createCommand(compiledCommand.getBaseCommand());
            cmd.setSubcommands(compiledCommand.getSubCommands().stream().map(this::createCommand).collect(Collectors.toList()));
            return cmd;
        }).collect(Collectors.toList()));

        apiCommand.register();
    }

    @Override
    public void broadcast(Component message) {
        Bukkit.broadcast(message);
    }

    @Override
    public GameMode getDefaultGameMode() {
        switch (getServer().getDefaultGameMode()) {
            case CREATIVE:
                return GameMode.CREATIVE;
            case SURVIVAL:
                return GameMode.SURVIVAL;
            case ADVENTURE:
                return GameMode.ADVENTURE;
            case SPECTATOR:
                return GameMode.SPECTATOR;
        }

        // Shouldn't even be possible
        return null;
    }
}
