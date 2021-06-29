package com.github.riku32.discordlink.Discord;

import com.freya02.botcommands.CommandsBuilder;
import com.github.riku32.discordlink.Discord.Listeners.CrosschatListener;
import com.github.riku32.discordlink.Discord.Listeners.VerificationListener;
import com.github.riku32.discordlink.DiscordLink;
import com.neovisionaries.ws.client.DualStackMode;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Bot {
    @Getter
    private JDA jda;

    @Getter
    private final Guild guild;

    // This is null if crosschat is disabled
    @Getter
    private TextChannel channel = null;

    public Bot(DiscordLink plugin, String token, String guildID, String ownerID, String channelID) {
        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(String.valueOf(token))
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
                    .addEventListeners(new VerificationListener(plugin));

            if (channelID != null) jdaBuilder.addEventListeners(new CrosschatListener(plugin));

            jda = jdaBuilder.build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            plugin.getLogger().severe("Unable to login to discord");
            e.printStackTrace();
        }

        this.guild = jda.getGuildById(guildID);
        if (this.guild == null)
            throw new IllegalArgumentException("Must have a valid guild ID");

        if (channelID != null)
            this.channel = guild.getTextChannelById(channelID);

        // Prefix option is ignored since we use slash commands
        CommandsBuilder commandsBuilder = CommandsBuilder.withPrefix("!", Long.parseLong(ownerID))
                //.setPermissionProvider(new PermissionManager(this.guild))
                .registerConstructorParameter(DiscordLink.class, ignored -> plugin)
                .disableHelpCommand(event -> {});

        try {
            commandsBuilder.build(jda, "com.github.riku32.discordlink.Discord.Commands");
        } catch (IOException exception) {
            plugin.getLogger().severe("Unable to register/update slash commands");
            exception.printStackTrace();
        }
    }
}
