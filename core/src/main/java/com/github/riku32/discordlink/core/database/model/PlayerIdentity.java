package com.github.riku32.discordlink.core.database.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerIdentity {
    private final Class<?> type;
    private final Object value;

    private PlayerIdentity(Object object) {
        type = object.getClass();
        value = object;
    }

    public static PlayerIdentity from(@NotNull UUID minecraftUuid) {
        return new PlayerIdentity(minecraftUuid);
    }

    public static PlayerIdentity from(@NotNull String discordId) {
        return new PlayerIdentity(discordId);
    }
    
    public void apply(Consumer<UUID> uuid, Consumer<String> discordId) {
        if (isUuid())
            uuid.accept((UUID) value);
        else
            discordId.accept((String) value);
    }

    public <T> T map(
        Function<UUID, ? extends T> uuid,
        Function<String, ? extends T> discordId
    ) {
        if (isUuid())
            return uuid.apply((UUID) value);
        else
            return discordId.apply((String) value);
    }

    public boolean isUuid() {
        return this.type == UUID.class;
    }

    public boolean isDiscordId() {
        return this.type == String.class;
    }

    public Object getValue() {
        return value;
    }
}
