package com.github.riku32.discordlink.core;

import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.commands.CommandLink;
import com.github.riku32.discordlink.core.config.Config;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.core.database.Verification;
import com.github.riku32.discordlink.core.framework.dependency.Injector;
import com.github.riku32.discordlink.core.listeners.PlayerStatusListener;
import com.github.riku32.discordlink.core.listeners.MoveListener;
import com.github.riku32.discordlink.core.locale.Locale;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.framework.PlatformPlugin;
import com.github.riku32.discordlink.core.framework.command.CompiledCommand;
import com.github.riku32.discordlink.core.util.MojangAPI;
import com.github.riku32.discordlink.core.util.skinrenderer.SkinRenderer;
import com.google.common.collect.ImmutableList;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Transaction;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class DiscordLink {
    public static Logger LOGGER;
    public static boolean DEBUG_MODE;

    private final PlatformPlugin plugin;
    private Database database;
    private Config config;
    private Locale locale;
    private Bot bot;
    private SkinRenderer renderContext;

    private final Set<PlatformPlayer> frozenPlayers = new HashSet<>();

    public DiscordLink(PlatformPlugin plugin) {
        this.plugin = plugin;
        LOGGER = plugin.getLogger();

        File configFile = new File(plugin.getDataDirectory(), "config.yml");
        if (!configFile.exists()) {
            try {
                Files.copy(Path.of(Objects.requireNonNull(getClass().getResource("config.yml")).getPath()), configFile.toPath());
                LOGGER.severe("Created a new configuration file, please fill in the file");
            } catch (IOException e) {
                LOGGER.severe("Unable to create configuration file");
                LOGGER.severe(e.getMessage());
            }
            disable(false);
            return;
        }

        try {
            this.config = new Config(new String(Files.readAllBytes(configFile.toPath())));
        } catch (NoSuchElementException | IOException e) {
            LOGGER.severe(e.getMessage());
            disable(false);
            return;
        }

        DEBUG_MODE = config.isDebugLog();

        if (DEBUG_MODE) {
            try {
                FileHandler handler = new FileHandler(new File(plugin.getDataDirectory(), "debuglog.txt").getPath());
                LOGGER.addHandler(handler);
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                disable(false);
                return;
            }
        }

        renderContext = new SkinRenderer(getClass().getClassLoader());
        renderContext.start();

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

        // Initialize database
        database = databaseInit();
        if (database == null) {
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

    private Database databaseInit() {
        // We need to load the class here to be able to use it
        // For some reason it does not work without this
        try {
            switch (config.getDatabaseSettings().platform) {
                case H2:
                    Class.forName("org.h2.Driver");
                    break;
                case POSTGRES:
                    Class.forName("org.postgresql.Driver");
                    break;
                case MYSQL:
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    break;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        // Create Database configurations
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl(config.getDatabaseSettings().getConnectionUri(plugin.getDataDirectory(), "database"));
        dataSourceConfig.setUsername(config.getDatabaseSettings().username);
        dataSourceConfig.setPassword(config.getDatabaseSettings().password);

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setDataSourceConfig(dataSourceConfig);
        dbConfig.setDefaultServer(true);
        dbConfig.setClasses(ImmutableList.of(PlayerInfo.class, Verification.class));

        // Set the current class loader to the plugin class loader, so we can initialize the database
        // This is a weird thing we need to do when using spigot specifically
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());

        // Initialize the database
        database = DatabaseFactory.create(dbConfig);

        // Run available migrations
        MigrationConfig migrationConfig = new MigrationConfig();
        migrationConfig.setMigrationPath("classpath:/dbmigration/" + config.getDatabaseSettings().platform.toString().toLowerCase());
        migrationConfig.load(new Properties());
        MigrationRunner runner = new MigrationRunner(migrationConfig);
        runner.run(database.dataSource());

        // Set the original class loader back
        Thread.currentThread().setContextClassLoader(previousClassLoader);

        return database;
    }

    private Injector createInjector() {
        Injector injector = new Injector();
        injector.registerDependency(PlatformPlugin.class, this.plugin);
        injector.registerNamedDependency("frozenPlayers", frozenPlayers);
        injector.registerDependency(Config.class, config);
        injector.registerDependency(Locale.class, locale);
        injector.registerDependency(Bot.class, bot);
        injector.registerDependency(SkinRenderer.class, renderContext);
        injector.registerDependency(MojangAPI.class, new MojangAPI());
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
        if (database != null) database.shutdown();
        if (renderContext != null) renderContext.finish();

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

    public PlatformPlugin getPlugin() {
        return plugin;
    }

    public Set<PlatformPlayer> getFrozenPlayers() {
        return frozenPlayers;
    }
}
