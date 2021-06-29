package com.github.riku32.discordlink;

import co.aikar.commands.BukkitCommandManager;
import com.github.riku32.discordlink.Commands.CancelCommand;
import com.github.riku32.discordlink.Commands.LinkCommand;
import com.github.riku32.discordlink.Discord.Bot;
import com.github.riku32.discordlink.Events.PlayerActivity;
import com.github.riku32.discordlink.Events.PlayerMove;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public final class DiscordLink extends JavaPlugin {
    @Getter
    private Bot bot;

    @Getter
    private Database database;

    @Getter
    private final ArrayList<UUID> frozenPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        this.bot = new Bot(this,
                String.valueOf(config.get("discord.token")),
                String.valueOf(config.get("discord.server_id")),
                String.valueOf(config.get("discord.owner_id"))
        );

        try {
            database = new Database(getDataFolder());
        } catch (SQLException e) {
            getLogger().severe("Unable to create database");
            e.printStackTrace();
        }

        // Add spigot commands
        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new LinkCommand());
        manager.registerCommand(new CancelCommand());

        // Add spigot events
        PlayerActivity playerActivity = new PlayerActivity(this);
        playerActivity.setPlayerCount(0); // Set initial status

        getServer().getPluginManager().registerEvents(playerActivity, this);
        getServer().getPluginManager().registerEvents(new PlayerMove(this), this);
    }

    @Override
    public void onDisable() {
        bot.shutdown();
    }
}
