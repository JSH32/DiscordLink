package com.github.riku32.discordlink.core;

import com.github.riku32.discordlink.core.commands.CommandTest;
import com.github.riku32.discordlink.core.database.Database;
import com.github.riku32.discordlink.core.eventbus.ListenerRegisterException;
import com.github.riku32.discordlink.core.events.JoinEvent;
import com.github.riku32.discordlink.core.platform.PlatformPlugin;
import com.github.riku32.discordlink.core.platform.command.CommandCompileException;
import com.github.riku32.discordlink.core.platform.command.CompiledCommand;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DiscordLink {
    @Getter
    private final PlatformPlugin plugin;

    @Getter
    private Database database;

    @Getter
    private Config config;

    public DiscordLink(PlatformPlugin plugin) {
        this.plugin = plugin;

        File configFile = new File(plugin.getDataDirectory(), "config.yml");
        if (!configFile.exists()) {
            try {
                Files.copy(Path.of(Objects.requireNonNull(getClass().getResource("config.yml")).getPath()), configFile.toPath());
                plugin.getLogger().severe("Created a new configuration file, please fill in the file");
            } catch (IOException e) {
                plugin.getLogger().severe("Unable to create configuration file");
                plugin.getLogger().severe(e.getMessage());
            }
            disable();
            return;
        }

        try {
            this.config = new Config(new String(Files.readAllBytes(configFile.toPath())));
        } catch (NoSuchElementException | IOException e) {
            plugin.getLogger().severe(e.getMessage());
            disable();
            return;
        }

        try {
            database = new Database(plugin.getDataDirectory());
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to create/start the database");
            plugin.getLogger().severe(e.getMessage());
            disable();
            return;
        }

        try {
            plugin.getEventBus().register(new JoinEvent());
        } catch (ListenerRegisterException e) {
            e.printStackTrace();
            disable();
        }

        try {
            plugin.registerCommand(new CompiledCommand(new CommandTest()));
        } catch (CommandCompileException e) {
            e.printStackTrace();
            disable();
        }
    }

    public void disable() {
        if (database != null) database.close();

        plugin.disable();
    }
}
