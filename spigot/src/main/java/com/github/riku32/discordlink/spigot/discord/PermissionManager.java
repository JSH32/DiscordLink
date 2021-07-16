package com.github.riku32.discordlink.spigot.discord;

import com.freya02.botcommands.CommandList;
import com.freya02.botcommands.PermissionProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This PermissionProvider should only permit one guild to use commands at a time, since this is meant for single server use
 */
public class PermissionManager implements PermissionProvider {
    private final Guild guild;

    /**
     * @param guild to allow commands for
     */
    public PermissionManager(Guild guild) {
        this.guild = guild;
    }

    public @NotNull CommandList getGuildCommands(String guildId) {
        if (this.guild.getId().equals(guildId))
            return CommandList.all();

        return CommandList.none();
    }

    public @NotNull Collection<CommandPrivilege> getPermissions(String commandName, String guildId) {
        return new ArrayList<>();
    }
}
