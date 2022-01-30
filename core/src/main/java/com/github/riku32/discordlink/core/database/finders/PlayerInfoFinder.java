package com.github.riku32.discordlink.core.database.finders;

import com.github.riku32.discordlink.core.database.PlayerInfo;
import io.ebean.Finder;

import java.util.Optional;
import java.util.UUID;

public class PlayerInfoFinder extends Finder<Long, PlayerInfo> {
    public PlayerInfoFinder() {
        super(PlayerInfo.class);
    }

    public Optional<PlayerInfo> byUuidOptional(UUID uuid) {
        return query()
                .where()
                .eq("uuid", uuid)
                .findOneOrEmpty();
    }

    public Optional<PlayerInfo> byDiscordIdOptional(String discordId) {
        return query()
                .where()
                .eq("discord_id", discordId)
                .findOneOrEmpty();
    }
}
