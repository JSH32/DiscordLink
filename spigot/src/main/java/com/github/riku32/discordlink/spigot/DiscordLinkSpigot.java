package com.github.riku32.discordlink.spigot;

import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.eventbus.EventBus;
import com.github.riku32.discordlink.core.platform.PlatformPlugin;
import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.core.platform.command.CompiledCommand;
import com.github.riku32.discordlink.spigot.events.MainListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

public final class DiscordLinkSpigot extends JavaPlugin implements PlatformPlugin {
//    @Getter
//    private Bot bot;
//
//    @Getter
//    private Database database;
//
//    @Getter
//    private final Set<UUID> frozenPlayers = new HashSet<>();
//
//    @Getter
//    private Config pluginConfig;
//
//    // Cross chat message relay from in-game chat
//    private WebhookClient messageRelay = null;

    private EventBus eventBus;

    @Override
    public void onEnable() {
        this.eventBus = new EventBus(getLogger());
        getServer().getPluginManager().registerEvents(new MainListener(eventBus), this);

        // This should automatically create and register the platform plugin
        new DiscordLink(this);
//        File configFile = new File(getDataFolder(), "config.yml");
//        if (!configFile.exists()) {
//            getLogger().warning("Created a new configuration file, please fill in the file");
//            saveDefaultConfig();
//            Bukkit.getPluginManager().disablePlugin(this);
//            return;
//        }
//
//        try {
//            this.pluginConfig = new Config(getConfig().saveToString());
//        } catch (NoSuchElementException | IOException e) {
//            getLogger().severe(e.getMessage());
//            Bukkit.getPluginManager().disablePlugin(this);
//            return;
//        }
//
//        try {
//            database = new Database(getDataFolder());
//        } catch (SQLException e) {
//            getLogger().severe("Unable to create/start the database");
//            e.printStackTrace();
//            Bukkit.getPluginManager().disablePlugin(this);
//            return;
//        }
//
//        try {
//            this.bot = new Bot(this,
//                    pluginConfig.getToken(),
//                    pluginConfig.getServerID(),
//                    pluginConfig.getOwnerID(),
//                    pluginConfig.getChannelID()
//            );
//        } catch (LoginException | InterruptedException e) {
//            getLogger().severe("Could not login to discord");
//            e.printStackTrace();
//            Bukkit.getPluginManager().disablePlugin(this);
//            return;
//        }
//
//        // Set initial player count status
//        bot.setPlayerCountStatus(0);
//
//        // Set in-game message relay
//        this.messageRelay = pluginConfig.getWebhook() == null ? null : WebhookClient.withUrl(pluginConfig.getWebhook());
//
//        // Add spigot commands
//        BukkitCommandManager manager = new BukkitCommandManager(this);
//        manager.registerCommand(new LinkCommand());
//        manager.registerCommand(new CancelCommand());
//
//        if (pluginConfig.isAllowUnlink())
//            manager.registerCommand(new UnlinkCommand());
//
//        // Add spigot events
//        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
//        getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
//        getServer().getPluginManager().registerEvents(new PlayerDeath(this), this);
//        getServer().getPluginManager().registerEvents(new PlayerMove(this), this);
//        getServer().getPluginManager().registerEvents(new PlayerChat(this, this.messageRelay), this);
    }

    @Override
    public PlatformPlayer getPlayer(UUID uuid) {
        return new SpigotPlayer(this.getServer().getPlayer(uuid));
    }

    @Override
    public PlatformPlayer getPlayer(String username) {
        return new SpigotPlayer(this.getServer().getPlayer(username));
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
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    private void registerDynamicCommand(String name, Command command) {
        String bukkitVersion = getServer().getClass().getPackage().getName();
        try {
            Class<?> clazzCraftServer = Class.forName("org.bukkit.craftbukkit." + bukkitVersion.substring(bukkitVersion.lastIndexOf('.') + 1) + ".CraftServer");
            Object craftServer = clazzCraftServer.cast(getServer());
            Object commandMap = craftServer.getClass().getDeclaredMethod("getCommandMap").invoke(craftServer);
            Map<String, Command> knownCommands = (Map<String, Command>) commandMap.getClass().getDeclaredMethod("getKnownCommands").invoke(commandMap);
            knownCommands.put(name, command);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerCommand(CompiledCommand compiledCommand) {
        Arrays.stream(compiledCommand.getBaseCommand().getAliases())
                .forEach(name -> registerDynamicCommand(name, new SpigotCommand(name, compiledCommand, this)));
    }
}
