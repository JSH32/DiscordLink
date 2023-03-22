package com.github.riku32.discordlink.core.commands;

import com.github.riku32.discordlink.core.bot.Bot;
import com.github.riku32.discordlink.core.database.PlayerInfo;
import com.github.riku32.discordlink.core.framework.PlatformOfflinePlayer;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;
import com.github.riku32.discordlink.core.framework.PlatformPlugin;
import com.github.riku32.discordlink.core.framework.command.CommandSender;
import com.github.riku32.discordlink.core.framework.command.annotation.Choice;
import com.github.riku32.discordlink.core.framework.command.annotation.Command;
import com.github.riku32.discordlink.core.framework.command.annotation.Default;
import com.github.riku32.discordlink.core.framework.dependency.annotation.Dependency;
import com.github.riku32.discordlink.core.locale.Locale;
import io.ebean.DB;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Command(aliases = {"unlink"})
public class CommandUnlink {
    @Dependency
    private Locale locale;

    @Dependency
    private PlatformPlugin plugin;

    @Dependency
    private Bot bot;

    @Default(userOnly = true)
    private boolean unlink(CommandSender sender) {
       return unlink(sender, plugin.getOfflinePlayer(sender.getUuid()), null);
    }

    @Command(
            aliases = {"something"}
    )
    private boolean something(CommandSender sender, PlatformPlayer player, @Choice({"uno", "dos", "tres"}) String hi) {
        return true;
    }

    /**
     * Unlink another player by their MC name
     */
    @Command(
            aliases = {"minecraft"},
            permission = "discord.unlink.player"
    )
    private boolean unlink(CommandSender sender, PlatformPlayer player) {
        return unlink(sender, plugin.getOfflinePlayer(player.getUuid()), null);
    }

    /**
     * Unlink another player by their discord tag
     */
    @Command(
            aliases = {"discord"},
            permission = "discord.unlink.player"
    )
    private boolean unlink(CommandSender sender, String tag) {
        Member member = bot.getGuild().getMemberByTag(tag);
        if (member != null) {
            return unlink(sender, null, member.getId());
        } else {
            sender.sendMessage(locale.getElement("unlink.account_invalid").error());
            return false;
        }
    }

    private boolean unlink(CommandSender sender, @Nullable PlatformOfflinePlayer offlinePlayer, @Nullable String discordId) {
        Optional<PlayerInfo> playerInfoOptional = Optional.empty();
        if (offlinePlayer != null) {
            playerInfoOptional = PlayerInfo.find.byUuidOptional(offlinePlayer.getUuid());
        } else if (discordId != null) {
            playerInfoOptional = PlayerInfo.find.byDiscordIdOptional(discordId);
        }

        if (playerInfoOptional.isEmpty()) {
            sender.sendMessage(locale.getElement("unlink.not_linked").error());
            return false;
        }

        // Remove linking data for the player.
        PlayerInfo playerInfo = playerInfoOptional.get();
        DB.delete(playerInfo);

        bot.getJda().retrieveUserById(playerInfo.discordId).queue(member -> {
            sender.sendMessage(locale.getElement("unlink.unlinked_player")
                    .set("username", plugin.getOfflinePlayer(playerInfo.uuid).getName())
                    .set("discord_tag", member.getAsTag())
                    .success());
        });

        return true;
    }
}
