package com.github.riku32.discordlink.spigot;

import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.framework.GameMode;
import com.github.riku32.discordlink.core.framework.eventbus.EventBus;
import com.github.riku32.discordlink.core.framework.PlatformPlugin;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.framework.command.CompiledCommand;
import com.github.riku32.discordlink.spigot.events.MainListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DiscordLinkSpigot extends JavaPlugin implements PlatformPlugin {
    public static DiscordLinkSpigot INSTANCE;

    private DiscordLink discordLink;
    private EventBus eventBus;
    private SpigotCommand commandManager;
    private PlayerRegistry playerRegistry;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.playerRegistry = new PlayerRegistry();
        getServer().getPluginManager().registerEvents(playerRegistry, this);

        this.eventBus = new EventBus();
        getServer().getPluginManager().registerEvents(new MainListener(eventBus, playerRegistry), this);

        this.commandManager = new SpigotCommand(this, playerRegistry);

        // This should automatically create and register the platform plugin
        discordLink = new DiscordLink(this);
        commandManager.setLocale(discordLink.getLocale());

        // Register command after initialization
        PluginCommand mainCommand = Objects.requireNonNull(this.getCommand("discord"));
        mainCommand.setExecutor(commandManager);
        mainCommand.setTabCompleter(commandManager);
    }

    @Override
    public PlatformPlayer getPlayer(UUID uuid) {
        Player player = this.getServer().getPlayer(uuid);
        return player == null ? null : playerRegistry.getPlayer(player);
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
        if (discordLink != null) discordLink.disable(true);

        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public void registerCommand(CompiledCommand compiledCommand) {
        this.commandManager.addCommand(compiledCommand);
    }

    @Override
    public void broadcast(String message) {
        Bukkit.broadcastMessage(message);
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

    @Override
    public boolean isOnline(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline();
    }

    @Override
    public boolean isOnline(String name) {
        Player player = Bukkit.getPlayer(name);
        return player != null && player.isOnline();
    }
}
