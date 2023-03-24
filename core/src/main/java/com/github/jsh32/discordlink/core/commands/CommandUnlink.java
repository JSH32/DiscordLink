package com.github.jsh32.discordlink.core.commands;

import com.github.jsh32.discordlink.core.bot.Bot;
import com.github.jsh32.discordlink.core.database.PlayerInfo;
import com.github.jsh32.discordlink.core.framework.PlatformOfflinePlayer;
import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.PlatformPlugin;
import com.github.jsh32.discordlink.core.framework.command.CommandSender;
import com.github.jsh32.discordlink.core.framework.command.annotation.Command;
import com.github.jsh32.discordlink.core.framework.command.annotation.Default;
import com.github.jsh32.discordlink.core.framework.dependency.annotation.Dependency;
import com.github.jsh32.discordlink.core.locale.Locale;
import io.ebean.DB;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Command(
        aliases = {"unlink"},
        permission = "discordlink.unlink"
)
public class CommandUnlink {
    @Dependency
    private Locale locale;

    @Dependency
    private PlatformPlugin plugin;

    @Dependency
    private Bot bot;

    @Default(userOnly = true)
    private void unlink(CommandSender sender) {
        unlink(sender, plugin.getOfflinePlayer(sender.getUuid()), null);
    }

    /**
     * Unlink another player by their MC name
     */
    @Command(
            aliases = {"minecraft"},
            permission = "discordlink.unlink.player"
    )
    private void unlink(CommandSender sender, PlatformPlayer player) {
        unlink(sender, plugin.getOfflinePlayer(player.getUuid()), null);
    }

    /**
     * Unlink another player by their discord tag
     */
    @Command(
            aliases = {"discord"},
            permission = "discordlink.unlink.player"
    )
    private void unlink(CommandSender sender, Member member) {
        unlink(sender, null, member.getId());
    }

    private void unlink(CommandSender sender, @Nullable PlatformOfflinePlayer offlinePlayer, @Nullable String discordId) {
        Optional<PlayerInfo> playerInfoOptional = Optional.empty();
        if (offlinePlayer != null) {
            playerInfoOptional = PlayerInfo.find.byUuidOptional(offlinePlayer.getUuid());
        } else if (discordId != null) {
            playerInfoOptional = PlayerInfo.find.byDiscordIdOptional(discordId);
        }

        if (playerInfoOptional.isEmpty()) {
            sender.sendMessage(locale.getElement("unlink.not_linked").error());
            return;
        }

        // Remove linking data for the player.
        PlayerInfo playerInfo = playerInfoOptional.get();

        if (!playerInfo.verified) {
            sender.sendMessage(locale.getElement("unlink.not_verified").error());
            return;
        }

        DB.delete(playerInfo);

        bot.getJda().retrieveUserById(playerInfo.discordId).queue(member -> {
            sender.sendMessage(locale.getElement("unlink.unlinked_player")
                    .set("username", plugin.getOfflinePlayer(playerInfo.uuid).getName())
                    .set("discord_tag", member.getAsTag())
                    .success());
        });
    }
}
