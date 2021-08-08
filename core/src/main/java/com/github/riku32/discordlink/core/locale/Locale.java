package com.github.riku32.discordlink.core.locale;

import java.util.Properties;

public class Locale {
    private final Properties properties;

    public Locale(Properties properties) {
        this.properties = properties;
    }

    public LocaleElement getElement(String identifier) {
        return new LocaleElement(properties.getOrDefault(identifier,
                "No identifier found in locale").toString());
    }
}