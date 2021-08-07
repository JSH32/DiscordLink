package com.github.riku32.discordlink.spigot.old.discord;

import com.freya02.botcommands.CommandsBuilder;
import com.freya02.botcommands.buttons.ButtonListener;
import com.github.riku32.discordlink.spigot.DiscordLinkSpigot;
import com.github.riku32.discordlink.spigot.old.discord.listeners.CrosschatListener;
import com.github.riku32.discordlink.spigot.old.discord.listeners.VerificationListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.neovisionaries.ws.client.DualStackMode;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.concurrent.*;

public class Bot {
//    @Getter
//    private final JDA jda;
//
//    @Getter
//    private final Guild guild;
//
//    // This is null if crosschat is disabled
//    @Getter
//    private TextChannel channel = null;
//
//    private final ExecutorService callbackThreadPool;
//
//    public Bot(DiscordLinkSpigot plugin, String token, String guildID, String ownerID, String channelID) throws LoginException, InterruptedException {
//        callbackThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), pool -> {
//            final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
//            worker.setName("DiscordLink - JDA Callback " + worker.getPoolIndex());
//            return worker;
//        }, null, true);
//
//        final ThreadFactory gatewayThreadFactory = new ThreadFactoryBuilder().setNameFormat("DiscordLink - JDA Gateway").build();
//        final ScheduledExecutorService gatewayThreadPool = Executors.newSingleThreadScheduledExecutor(gatewayThreadFactory);
//
//        final ThreadFactory rateLimitThreadFactory = new ThreadFactoryBuilder().setNameFormat("DiscordLink - JDA Rate Limit").build();
//        final ScheduledExecutorService rateLimitThreadPool = new ScheduledThreadPoolExecutor(5, rateLimitThreadFactory);
//
//        JDABuilder jdaBuilder = JDABuilder.createDefault(String.valueOf(token))
//                .setChunkingFilter(ChunkingFilter.ALL)
//                .setMemberCachePolicy(MemberCachePolicy.ALL)
//                .setWebsocketFactory(new WebSocketFactory()
//                        .setDualStackMode(DualStackMode.IPV4_ONLY)
//                )
//                .setCallbackPool(callbackThreadPool, false)
//                .setGatewayPool(gatewayThreadPool, true)
//                .setRateLimitPool(rateLimitThreadPool, true)
//                .setAutoReconnect(true)
//                .setBulkDeleteSplittingEnabled(false)
//                .enableIntents(GatewayIntent.GUILD_MEMBERS,
//                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
//                        GatewayIntent.DIRECT_MESSAGES)
//                .addEventListeners(new VerificationListener(plugin));
//
//
//        if (plugin.getPluginConfig().isCrossChatEnabled()) jdaBuilder.addEventListeners(new CrosschatListener(plugin));
//
//        jda = jdaBuilder.build().awaitReady();
//
//        this.guild = jda.getGuildById(guildID);
//        if (this.guild == null)
//            throw new IllegalArgumentException("Must have a valid guild ID");
//
//        if (channelID != null)
//            this.channel = guild.getTextChannelById(channelID);
//
//        // Prefix option is ignored since we use slash commands
//        CommandsBuilder commandsBuilder = CommandsBuilder.withPrefix("!", Long.parseLong(ownerID))
//                .setPermissionProvider(new PermissionManager(this.guild))
//                .registerConstructorParameter(DiscordLinkSpigot.class, ignored -> plugin)
//                // Disable both help commands, we only use a few slash commands so its self explanatory
//                .disableSlashHelpCommand()
//                .disableHelpCommand(event -> {});
//
//        try {
//            commandsBuilder.build(jda, "com.github.riku32.discordlink.spigot.old.discord.commands");
//
//        } catch (IOException exception) {
//            plugin.getLogger().severe("Unable to register/update slash commands");
//            exception.printStackTrace();
//        }
//
//        // Delete bot commands framework listener
//        for (Object listener : jda.getRegisteredListeners())
//            if (listener instanceof ButtonListener)
//                jda.removeEventListener(listener);
//    }
//
//    public void setPlayerCountStatus(int playerCount) {
//        jda.getPresence().setActivity(Activity.watching(String.format("%d people play minecraft", playerCount)));
//    }
//
//    public void shutdown() {
//        if (jda != null) jda.shutdown();
//        if (callbackThreadPool != null) callbackThreadPool.shutdownNow();
//    }
}
