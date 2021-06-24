package com.github.riku32.discordlink;

import co.aikar.commands.BukkitCommandManager;
import com.github.riku32.discordlink.Commands.LinkCommand;
import com.github.riku32.discordlink.Events.PlayerActivity;
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

public final class DiscordLink extends JavaPlugin {
    @Getter
    private JDA jda;

    @Getter
    private Database database;

    @Getter
    private String guildID;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        guildID = String.valueOf(config.get("discord.server_id"));

        try {
            jda = JDABuilder.createDefault((String) config.get("discord.token"))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .build();
        } catch (LoginException e) {
            getLogger().severe("Invalid discord token");
            e.printStackTrace();
        }

        try {
            database = new Database(getDataFolder());
        } catch (SQLException e) {
            getLogger().severe("Unable to create database");
            e.printStackTrace();
        }

        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new LinkCommand());

        getServer().getPluginManager().registerEvents(new PlayerActivity(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
