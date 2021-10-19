package com.github.riku32.discordlink.core.database.model;

import java.util.UUID;

public class PlayerInfo {
    private final String discordID;
    private final UUID uuid;
    private final boolean verified;

    public PlayerInfo(String discordID, UUID uuid, boolean verified) {
        this.discordID = discordID;
        this.uuid = uuid;
        this.verified = verified;
    }

    public String getDiscordID() {
        return discordID;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isVerified() {
        return verified;
    }
}
