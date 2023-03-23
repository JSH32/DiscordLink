package com.github.jsh32.discordlink.core.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class LocaleElement {
    private String string;

    public LocaleElement(String string) {
        this.string = string;
    }

    /**
     * Set a variable in the locale string
     *
     * NOTE: Once you set a variable you can no longer set it
     */
    public LocaleElement set(String variable, String value) {
        string = string.replaceAll(String.format("%%%s%%", variable), value);
        return this;
    }

    public Component info() { return MiniMessage.miniMessage().deserialize("<gray>[<bold><aqua>!</bold><gray>] <reset>" + string); }

    public Component success() {return MiniMessage.miniMessage().deserialize("<gray>[<bold><green>✔</bold><gray>] <reset>" + string); }

    public Component error() {return MiniMessage.miniMessage().deserialize("<gray>[<bold><red>✗</bold><gray>] <reset>" + string); }

    public Component component(boolean prefix) {
        return MiniMessage.miniMessage().deserialize(prefix
                ? "<gray>[<bold><blue>D<dark_green>L<gray></bold>] <reset>" + string
                : string
        );
    }

    @Override
    public String toString() { return string; }
}
