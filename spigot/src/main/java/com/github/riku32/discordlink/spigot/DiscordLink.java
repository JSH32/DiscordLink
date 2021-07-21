package com.github.riku32.discordlink.spigot;

import club.minnced.discord.webhook.WebhookClient;
import co.aikar.commands.BukkitCommandManager;
import com.github.riku32.discordlink.core.Config;
import com.github.riku32.discordlink.spigot.commands.CancelCommand;
import com.github.riku32.discordlink.core.database.Database;
import com.github.riku32.discordlink.spigot.discord.Bot;
import com.github.riku32.discordlink.spigot.events.*;
import com.github.riku32.discordlink.spigot.commands.LinkCommand;
import com.github.riku32.discordlink.spigot.commands.UnlinkCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public final class DiscordLink extends JavaPlugin {
    @Getter
    private Bot bot;

    @Getter
    private Database database;

    @Getter
    private final Set<UUID> frozenPlayers = new HashSet<>();

    @Getter
    private Config pluginConfig;

    // Cross chat message relay from in-game chat
    private WebhookClient messageRelay = null;

    @Override
    public void onEnable() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().warning("Created a new configuration file, please fill in the file");
            saveDefaultConfig();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            this.pluginConfig = new Config(getConfig().saveToString());
        } catch (NoSuchElementException | IOException e) {
            getLogger().severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            database = new Database(getDataFolder());
        } catch (SQLException e) {
            getLogger().severe("Unable to create/start the database");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            this.bot = new Bot(this,
                    pluginConfig.getToken(),
                    pluginConfig.getServerID(),
                    pluginConfig.getOwnerID(),
                    pluginConfig.getChannelID()
            );
        } catch (LoginException | InterruptedException e) {
            getLogger().severe("Could not login to discord");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Set initial player count status
        bot.setPlayerCountStatus(0);

        // Set in-game message relay
        this.messageRelay = pluginConfig.getWebhook() == null ? null : WebhookClient.withUrl(pluginConfig.getWebhook());

        // Add spigot commands
        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new LinkCommand());
        manager.registerCommand(new CancelCommand());

        if (pluginConfig.isAllowUnlink())
            manager.registerCommand(new UnlinkCommand());

        // Add spigot events
        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeath(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMove(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChat(this, this.messageRelay), this);
    }

    @Override
    public void onDisable() {
        // Shut down relay executor
        if (messageRelay != null) messageRelay.close();

        if (bot != null) bot.shutdown();
    }
}
