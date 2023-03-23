package com.github.jsh32.discordlink.core;

import club.minnced.discord.webhook.WebhookClient;
import com.github.jsh32.discordlink.core.bot.Bot;
import com.github.jsh32.discordlink.core.commands.CommandLink;
import com.github.jsh32.discordlink.core.commands.CommandUnlink;
import com.github.jsh32.discordlink.core.config.Config;
import com.github.jsh32.discordlink.core.database.PlayerInfo;
import com.github.jsh32.discordlink.core.database.Verification;
import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.PlatformPlugin;
import com.github.jsh32.discordlink.core.framework.command.CommandCompileException;
import com.github.jsh32.discordlink.core.framework.command.CompiledCommand;
import com.github.jsh32.discordlink.core.framework.dependency.Injector;
import com.github.jsh32.discordlink.core.framework.dependency.exceptions.DependencyNotFoundException;
import com.github.jsh32.discordlink.core.framework.dependency.exceptions.DependencyNotNullException;
import com.github.jsh32.discordlink.core.listeners.ChatListener;
import com.github.jsh32.discordlink.core.listeners.MoveListener;
import com.github.jsh32.discordlink.core.listeners.PlayerStatusListener;
import com.github.jsh32.discordlink.core.locale.Locale;
import com.google.common.collect.ImmutableList;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import net.kyori.adventure.text.Component;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiscordLink {
    public static Logger LOGGER;
    public static boolean DEBUG_MODE;

    private final PlatformPlugin plugin;
    private Database database;
    private Config config;
    private com.github.jsh32.discordlink.core.locale.Locale locale;
    private Bot bot;

    private Injector injector;

    private final Set<PlatformPlayer> frozenPlayers = new HashSet<>();

    public DiscordLink(PlatformPlugin plugin) {
        this.plugin = plugin;
        LOGGER = plugin.getLogger();

        File configFile = new File(plugin.getDataDirectory(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                Files.copy(
                    Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("config.yml")),
                    configFile.toPath()
                );

                LOGGER.severe("Created a new configuration file, please fill in the file");
            } catch (IOException e) {
                LOGGER.severe("Unable to create configuration file");
                LOGGER.severe(e.toString());
            }
            disable(false);
            return;
        }

        try {
            this.config = new Config(new String(Files.readAllBytes(configFile.toPath())));
        } catch (NoSuchElementException | IOException e) {
            LOGGER.severe(e.toString());
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

        try {
            Properties prop = new Properties();
            InputStream localeStream = getClass().getClassLoader().getResourceAsStream("locale/en-US.properties");
            prop.load(localeStream);
            Objects.requireNonNull(localeStream).close();
            locale = new com.github.jsh32.discordlink.core.locale.Locale(prop);
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

        WebhookClient messageRelay = config.getWebhook() == null ? null : WebhookClient.withUrl(config.getWebhook());

        try {
            PlayerStatusListener playerStatusListener = new PlayerStatusListener();
            injector = createInjector();
            injector.injectDependencies(playerStatusListener);
            plugin.getEventBus().register(playerStatusListener);

            plugin.getEventBus().register(new ChatListener(this, messageRelay, bot));
            plugin.getEventBus().register(new MoveListener(frozenPlayers));
        } catch (Exception e) {
            e.printStackTrace();
            disable(false);
            return;
        }

        try {
            registerCommand(new CommandLink());
            registerCommand(new CommandUnlink());
        } catch (Exception e) {
            e.printStackTrace();
            disable(false);
        }
    }

    private void registerCommand(Object... commands) {
        ArrayList<CompiledCommand> compiledCommands = new ArrayList<>();
        for (var command : commands) {
            try {
                injector.injectDependencies(command);
                compiledCommands.add(new CompiledCommand(command));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        plugin.registerCommands(compiledCommands);
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
        injector.registerDependency(com.github.jsh32.discordlink.core.locale.Locale.class, locale);
        injector.registerDependency(Bot.class, bot);
        return injector;
    }

    public void broadcast(Component message) {
        plugin.broadcast(message);
    }

    /**
     * Disable the plugin
     *
     * @param triggerShutdown should this trigger a shutdown in respective platforms.
     */
    public void disable(boolean triggerShutdown) {
        if (bot != null) bot.shutdown();
        if (database != null) database.shutdown();

        // This is to prevent a recursive loop of disable being called.
        // Don't call shutdown on plugin unless triggered.
        if (triggerShutdown) plugin.disable();
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
