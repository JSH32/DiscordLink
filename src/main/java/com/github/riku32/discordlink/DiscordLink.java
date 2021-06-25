package com.github.riku32.discordlink;

import co.aikar.commands.BukkitCommandManager;
import com.github.riku32.discordlink.Commands.CancelCommand;
import com.github.riku32.discordlink.Commands.LinkCommand;
import com.github.riku32.discordlink.Events.PlayerActivity;
import com.github.riku32.discordlink.Events.PlayerMove;
import com.github.riku32.discordlink.Listeners.VerificationListener;
import com.neovisionaries.ws.client.DualStackMode;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public final class DiscordLink extends JavaPlugin {
    @Getter
    private JDA jda;

    @Getter
    private Database database;

    @Getter
    private String guildID;

    @Getter
    private final ArrayList<UUID> frozenPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        guildID = String.valueOf(config.get("discord.server_id"));

        try {
            jda = JDABuilder.createDefault(String.valueOf(config.get("discord.token")))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setWebsocketFactory(new WebSocketFactory()
                            .setDualStackMode(DualStackMode.IPV4_ONLY)
                    )
                    .setAutoReconnect(true)
                    .setBulkDeleteSplittingEnabled(false)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                            GatewayIntent.DIRECT_MESSAGES)
                    .addEventListeners(new VerificationListener(this))
                    .build();
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            getLogger().severe("Unable to login to discord");
            e.printStackTrace();
        }

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
        jda.shutdown();
    }
}
