package com.github.riku32.discordlink.core.database.managers;

import com.github.riku32.discordlink.core.database.DataException;
import com.github.riku32.discordlink.core.database.model.PlayerIdentity;
import com.github.riku32.discordlink.core.database.model.PlayerInfo;

import java.util.Optional;
import java.util.UUID;

public interface PlayerManager {
    Optional<PlayerInfo> getPlayerInfo(PlayerIdentity playerIdentity) throws DataException;
    void createPlayer(UUID uuid, String discordId, String verificationMessageId) throws DataException;
    void deletePlayer(PlayerIdentity playerIdentity) throws DataException;
    void verifyPlayer(PlayerIdentity playerIdentity) throws DataException;
    boolean isPlayerLinked(PlayerIdentity playerIdentity) throws DataException;
    String getVerificationMessage(PlayerIdentity playerIdentity) throws DataException;
}
