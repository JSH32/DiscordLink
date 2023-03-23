package com.github.jsh32.discordlink.spigot;

import com.github.jsh32.discordlink.core.DiscordLink;
import com.github.jsh32.discordlink.core.framework.GameMode;
import com.github.jsh32.discordlink.core.framework.PlatformOfflinePlayer;
import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.PlatformPlugin;
import com.github.jsh32.discordlink.core.framework.command.CompiledCommand;
import com.github.jsh32.discordlink.core.framework.eventbus.EventBus;
import com.github.jsh32.discordlink.spigot.events.MainListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DiscordLinkSpigot extends JavaPlugin implements PlatformPlugin {
    public static DiscordLinkSpigot INSTANCE;

    private DiscordLink discordLink;
    private EventBus eventBus;
    private PlayerRegistry playerRegistry;

    private SpigotCommand command;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.playerRegistry = new PlayerRegistry();
        getServer().getPluginManager().registerEvents(playerRegistry, this);

        this.eventBus = new EventBus();
        getServer().getPluginManager().registerEvents(new MainListener(eventBus, playerRegistry), this);

        // This should automatically create and register the platform plugin
        discordLink = new DiscordLink(this);
        discordLink.registerHandlers();
    }

    public DiscordLink getDiscordLink() {
        return discordLink;
    }

    @Override
    public void onDisable() {
        command.onDisable();
        discordLink.disable(false);
    }

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

    @Override
    public void registerCommands(List<CompiledCommand> compiledCommand) {
        if (command != null)
            command.onDisable();

        command = new SpigotCommand(this, playerRegistry);
        command.registerCommands(compiledCommand);
    }

    @Override
    public void broadcast(Component message) {
        Bukkit.broadcast(message);
    }

    @Override
    public GameMode getDefaultGameMode() {
        return switch (getServer().getDefaultGameMode()) {
            case CREATIVE -> GameMode.CREATIVE;
            case SURVIVAL -> GameMode.SURVIVAL;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
        };
    }
}
