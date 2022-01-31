package com.github.riku32.discordlink.core.database.finders;

import com.github.riku32.discordlink.core.database.Verification;
import com.github.riku32.discordlink.core.database.enums.VerificationType;
import io.ebean.Finder;

import java.util.Optional;

public class VerificationFinder extends Finder<Long, Verification> {
    public VerificationFinder() {
        super(Verification.class);
    }

    public Optional<Verification> byValueAndType(VerificationType type, String value) {
        return query()
                .fetch("player")
                .where()
                .eq("type", type.getValue())
                .eq("value", value)
                .findOneOrEmpty();
    }
}
