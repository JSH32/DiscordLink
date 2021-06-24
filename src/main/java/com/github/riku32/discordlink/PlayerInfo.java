package com.github.riku32.discordlink;

import lombok.Data;

@Data
public class PlayerInfo {
    private final String discordID;
    private final boolean verified;

    public PlayerInfo(String discordID, boolean verified) {
        this.discordID = discordID;
        this.verified = verified;
    }
}
