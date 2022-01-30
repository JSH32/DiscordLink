package com.github.riku32.discordlink.core.database;

import com.github.riku32.discordlink.core.database.finders.PlayerInfoFinder;
import io.ebean.Model;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.NotNull;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "players")
public class PlayerInfo extends Model {
    public static PlayerInfoFinder find = new PlayerInfoFinder();

    @Id
    long id;

    @NotNull
    @Column(unique = true)
    @DbComment("Mojang assigned UUID")
    public UUID uuid;

    @NotNull
    @DbComment("Weather the user has verified their discord account linkage")
    public boolean verified = false;

    @NotNull
    @Column(unique = true)
    @DbComment("Discord snowflake ID")
    public String discordId;

    @Unique
    @DbComment("ID of the discord message sent during verification")
    public String verificationMessageId;

    public PlayerInfo(
            @org.jetbrains.annotations.NotNull UUID uuid,
            @org.jetbrains.annotations.NotNull String discordId,
            @org.jetbrains.annotations.NotNull String verificationMessageId) {
        this.uuid = uuid;
        this.discordId = discordId;
        this.verificationMessageId = verificationMessageId;
    }

    public PlayerInfo setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}