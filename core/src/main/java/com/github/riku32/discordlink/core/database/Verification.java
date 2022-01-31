package com.github.riku32.discordlink.core.database;

import com.github.riku32.discordlink.core.database.enums.VerificationType;
import com.github.riku32.discordlink.core.database.finders.PlayerInfoFinder;
import com.github.riku32.discordlink.core.database.finders.VerificationFinder;
import io.ebean.Model;
import io.ebean.annotation.*;
import io.ebean.annotation.ConstraintMode;

import javax.persistence.*;

@Entity
@Table(name = "verifications")
public class Verification extends Model {
    public static VerificationFinder find = new VerificationFinder();

    @Id
    long id;

    @NotNull
    @Column(unique = true)
    @OneToOne
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    @DbComment("Player that created this verification")
    public PlayerInfo player;

    @DbComment("Type of verification")
    public VerificationType type;

    @Column(unique = true)
    @DbComment("Value of the verification, this is a messageId in message_reaction and a code in code")
    public String value;

    /**
     * Create a new verification object in the database<br>
     * One of the two fields after player must be null.
     *
     * @param player to create verification for
     * @param verificationType type of verification being used
     * @param verificationValue value of the verification, this is a messageId in message_reaction and a code in code
     */
    public Verification(
            @org.jetbrains.annotations.NotNull PlayerInfo player,
            VerificationType verificationType, String verificationValue) {
        this.player = player;
        this.type = verificationType;
        this.value = verificationValue;
    }
}
