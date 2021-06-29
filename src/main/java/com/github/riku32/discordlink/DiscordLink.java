package com.github.riku32.discordlink;

import club.minnced.discord.webhook.WebhookClient;
import co.aikar.commands.BukkitCommandManager;
import com.github.riku32.discordlink.commands.CancelCommand;
import com.github.riku32.discordlink.commands.LinkCommand;
import com.github.riku32.discordlink.discord.Bot;
import com.github.riku32.discordlink.events.PlayerActivity;
import com.github.riku32.discordlink.events.PlayerChat;
import com.github.riku32.discordlink.events.PlayerMove;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
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

    // Crosschat message relay from in-game chat
    private WebhookClient messageRelay = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.pluginConfig = new Config(super.getConfig());

        try {
            database = new Database(getDataFolder());
        } catch (SQLException e) {
            getLogger().severe("Unable to create database");
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

        // Set in-game message relay
        this.messageRelay = pluginConfig.getWebhook() == null ? null : WebhookClient.withUrl(pluginConfig.getWebhook());

        // Add spigot commands
        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new LinkCommand());
        manager.registerCommand(new CancelCommand());

        // Add spigot events
        PlayerActivity playerActivity = new PlayerActivity(this);
        playerActivity.setPlayerCount(0); // Set initial status

        getServer().getPluginManager().registerEvents(playerActivity, this);
        getServer().getPluginManager().registerEvents(new PlayerMove(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChat(this, this.messageRelay), this);
    }

    @Override
    public void onDisable() {
        // Shut down relay executor
        if (this.messageRelay != null) this.messageRelay.close();

        bot.shutdown();
    }
}
