package com.github.jsh32.discordlink.core.bot;

import com.github.jsh32.discordlink.core.bot.listeners.CrossChatListener;
import com.github.jsh32.discordlink.core.bot.listeners.VerificationListener;
import com.github.jsh32.discordlink.core.DiscordLink;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.neovisionaries.ws.client.DualStackMode;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.util.concurrent.*;

public class Bot {
    private final JDA jda;
    private final Guild guild;
    private TextChannel channel = null;

    private final ExecutorService callbackThreadPool;

    /**
     * Create the discord bot portion of the plugin
     *
     * @param discordLink the main plugin to get information about
     */
    public Bot(DiscordLink discordLink) throws LoginException, InterruptedException {
        callbackThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), pool -> {
            final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName("DiscordLink - JDA Callback " + worker.getPoolIndex());
            return worker;
        }, null, true);

        final ThreadFactory gatewayThreadFactory = new ThreadFactoryBuilder().setNameFormat("DiscordLink - JDA Gateway").build();
        final ScheduledExecutorService gatewayThreadPool = Executors.newSingleThreadScheduledExecutor(gatewayThreadFactory);

        final ThreadFactory rateLimitThreadFactory = new ThreadFactoryBuilder().setNameFormat("DiscordLink - JDA Rate Limit").build();
        final ScheduledExecutorService rateLimitThreadPool = new ScheduledThreadPoolExecutor(5, rateLimitThreadFactory);

        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(discordLink.getConfig().getToken())
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setWebsocketFactory(new WebSocketFactory()
                            .setDualStackMode(DualStackMode.IPV4_ONLY)
                    ).setCallbackPool(callbackThreadPool, false)
                    .setGatewayPool(gatewayThreadPool, true)
                    .setRateLimitPool(rateLimitThreadPool, true)
                    .setAutoReconnect(true)
                    .setBulkDeleteSplittingEnabled(false)
                    .addEventListeners(new VerificationListener(this, discordLink))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                            GatewayIntent.DIRECT_MESSAGES);

            if (discordLink.getConfig().isCrossChatEnabled()) jdaBuilder.addEventListeners(new CrossChatListener(discordLink, this));

            jda = jdaBuilder.build().awaitReady();
        } catch (InterruptedException e) {
            shutdown();
            throw e;
        }

        guild = jda.getGuildById(discordLink.getConfig().getServerID());
        if (guild == null) {
            shutdown();
            throw new IllegalStateException("Must have a valid guild ID");
        }

        if (discordLink.getConfig().isCrossChatEnabled()) {
            if (discordLink.getConfig().getChannelID() == null) {
                shutdown();
                throw new IllegalArgumentException("Must have a valid channel ID");
            }

            channel = guild.getTextChannelById(discordLink.getConfig().getChannelID());
            if (channel == null) {
                shutdown();
                throw new IllegalArgumentException(String.format("Channel ID must exist within %s", guild.getName()));
            }
        }
    }

    public void shutdown() {
        if (jda != null) jda.shutdownNow();
        if (callbackThreadPool != null) callbackThreadPool.shutdownNow();
    }

    public JDA getJda() {
        return jda;
    }

    public Guild getGuild() {
        return guild;
    }

    public TextChannel getChannel() {
        return channel;
    }
}
