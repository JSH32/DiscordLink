package com.github.riku32.discordlink.core.database.enums;

import io.ebean.annotation.DbEnumValue;

public enum VerificationType {
    MESSAGE_REACTION("message_reaction"),
    CODE("code");

    public final String label;
    VerificationType(String label) {
        this.label = label;
    }

    @DbEnumValue
    public String getValue() {
        return label;
    }
}
