package com.github.riku32.discordlink.core;

import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.commands.CommandLink;
import com.github.riku32.discordlink.core.database.managers.PlayerManager;
import com.github.riku32.discordlink.core.database.sources.SqliteDB;
import com.github.riku32.discordlink.core.framework.dependency.DependencyNotFoundException;
import com.github.riku32.discordlink.core.framework.dependency.Injector;
import com.github.riku32.discordlink.core.framework.eventbus.ListenerRegisterException;
import com.github.riku32.discordlink.core.listeners.PlayerStatusListener;
import com.github.riku32.discordlink.core.listeners.MoveListener;
import com.github.riku32.discordlink.core.locale.Locale;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.framework.PlatformPlugin;
import com.github.riku32.discordlink.core.framework.command.CommandCompileException;
import com.github.riku32.discordlink.core.framework.command.CompiledCommand;
import com.github.riku32.discordlink.core.util.MojangAPI;

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
    private SqliteDB sqliteDB;
    private Config config;
    private Locale locale;
    private Bot bot;

    private final Set<PlatformPlayer> frozenPlayers = new HashSet<>();

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
            sqliteDB = new SqliteDB(plugin.getDataDirectory());
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
            disable(false);
            return;
        }

        try {
            bot = new Bot(this);
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            disable(false);
            return;
        }

        Injector injector = createInjector();

        try {
            PlayerStatusListener playerStatusListener = new PlayerStatusListener();
            injector.injectDependencies(playerStatusListener);
            plugin.getEventBus().register(playerStatusListener);

            plugin.getEventBus().register(new MoveListener(frozenPlayers));
        } catch (Exception e) {
            e.printStackTrace();
            disable(false);
            return;
        }

        try {
            CommandLink commandLink = new CommandLink();
            injector.injectDependencies(commandLink);

            plugin.registerCommand(new CompiledCommand(commandLink));
        } catch (Exception e) {
            e.printStackTrace();
            disable(false);
        }
    }

    private Injector createInjector() {
        Injector injector = new Injector();
        injector.registerDependency(PlatformPlugin.class, this.plugin);
        injector.registerNamedDependency("frozenPlayers", frozenPlayers);
        injector.registerDependency(PlayerManager.class, sqliteDB);
        injector.registerDependency(Config.class, config);
        injector.registerDependency(Locale.class, locale);
        injector.registerDependency(Bot.class, bot);
        return injector;
    }

    public void broadcast(String message) {
        plugin.broadcast(message);
    }

    /**
     * Disable the plugin
     *
     * @param fromPluginShutdown was this called from the {@link PlatformPlugin} implementation?
     */
    public void disable(boolean fromPluginShutdown) {
        if (bot != null) bot.shutdown();
        if (sqliteDB != null) sqliteDB.close();

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

    public SqliteDB getDatabase() {
        return sqliteDB;
    }

    public PlatformPlugin getPlugin() {
        return plugin;
    }

    public Set<PlatformPlayer> getFrozenPlayers() {
        return frozenPlayers;
    }
}
