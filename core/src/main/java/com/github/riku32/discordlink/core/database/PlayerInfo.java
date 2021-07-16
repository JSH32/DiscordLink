package com.github.riku32.discordlink.core.database;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerInfo {
    private final String discordID;
    private final UUID uuid;
    private final boolean verified;

    public PlayerInfo(String discordID, UUID uuid, boolean verified) {
        this.discordID = discordID;
        this.uuid = uuid;
        this.verified = verified;
    }
}
