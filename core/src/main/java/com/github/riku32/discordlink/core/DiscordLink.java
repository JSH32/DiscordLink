package com.github.riku32.discordlink.core;

import com.github.riku32.discordlink.core.commands.CommandLink;
import com.github.riku32.discordlink.core.database.Database;
import com.github.riku32.discordlink.core.eventbus.ListenerRegisterException;
import com.github.riku32.discordlink.core.events.JoinEvent;
import com.github.riku32.discordlink.core.locale.Locale;
import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.core.platform.PlatformPlugin;
import com.github.riku32.discordlink.core.platform.command.CommandCompileException;
import com.github.riku32.discordlink.core.platform.command.CompiledCommand;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

public class DiscordLink {
    private final PlatformPlugin plugin;
    private Database database;
    private Config config;
    private Locale locale;

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
            disable(false);
            return;
        }

        try {
            this.config = new Config(new String(Files.readAllBytes(configFile.toPath())));
        } catch (NoSuchElementException | IOException e) {
            plugin.getLogger().severe(e.getMessage());
            disable(false);
            return;
        }

        try {
            database = new Database(plugin.getDataDirectory());
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to create/start the database");
            plugin.getLogger().severe(e.getMessage());
            disable(false);
            return;
        }

        try {
            Properties prop = new Properties();
            InputStream localeStream = getClass().getClassLoader().getResourceAsStream("locale/en-US.properties");
            prop.load(localeStream);
            Objects.requireNonNull(localeStream).close();
            locale = new Locale(prop);
        } catch (Exception e) {
            e.printStackTrace();
            disable();
            return;
        }

        try {
            plugin.getEventBus().register(new JoinEvent());
        } catch (ListenerRegisterException e) {
            e.printStackTrace();
            disable(false);
            return;
        }

        try {
            plugin.registerCommand(new CompiledCommand(new CommandLink(this)));
        } catch (CommandCompileException e) {
            e.printStackTrace();
            disable(false);
        }
    }

    /**
     * Disable the plugin
     *
     * @param fromPluginShutdown was this called from the {@link PlatformPlugin} implementation?
     */
    public void disable(boolean fromPluginShutdown) {
        if (bot != null) bot.shutdown();
        if (database != null) database.close();

        // Only call shutdown on the main plugin if shutdown was called within the plugin implementation.
        // This is to prevent a recursive loop of disable being called
        if (fromPluginShutdown) plugin.disable();
    }

    public Locale getLocale() {
        return locale;
    }

    public Config getConfig() {
        return config;
    }

    public Database getDatabase() {
        return database;
    }

    public PlatformPlugin getPlugin() {
        return plugin;
    }
}
