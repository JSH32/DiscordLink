package com.github.riku32.discordlink;

import club.minnced.discord.webhook.WebhookClient;
import co.aikar.commands.BukkitCommandManager;
import com.github.riku32.discordlink.Commands.CancelCommand;
import com.github.riku32.discordlink.Commands.LinkCommand;
import com.github.riku32.discordlink.Discord.Bot;
import com.github.riku32.discordlink.Events.PlayerActivity;
import com.github.riku32.discordlink.Events.PlayerChat;
import com.github.riku32.discordlink.Events.PlayerMove;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public final class DiscordLink extends JavaPlugin {
    @Getter
    private Bot bot;

    @Getter
    private Database database;

    @Getter
    private final ArrayList<UUID> frozenPlayers = new ArrayList<>();

    // Crosschat message relay from in-game chat
    private WebhookClient messageRelay = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        this.bot = new Bot(this,
                String.valueOf(config.get("discord.token")),
                String.valueOf(config.get("discord.server_id")),
                String.valueOf(config.get("discord.owner_id")),
                !(boolean) Objects.requireNonNull(config.get("chat.enabled"))
                        || !(boolean) Objects.requireNonNull(config.get("chat.crosschat.enabled"))
                        ? null : String.valueOf(config.get("chat.crosschat.channel_id"))
        );

        try {
            database = new Database(getDataFolder());
        } catch (SQLException e) {
            getLogger().severe("Unable to create database");
            e.printStackTrace();
        }

        // Set in-game message relay
        if ((boolean) Objects.requireNonNull(config.get("chat.enabled")) && (boolean) Objects.requireNonNull(config.get("chat.crosschat.enabled")))
            if (config.get("chat.crosschat.webhook") != null)
                this.messageRelay = WebhookClient.withUrl(String.valueOf(config.get("chat.crosschat.webhook")));

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

        bot.getJda().shutdown();
    }
}
