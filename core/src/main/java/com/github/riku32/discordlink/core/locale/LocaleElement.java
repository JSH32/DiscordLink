package com.github.riku32.discordlink.core.locale;

import com.github.riku32.discordlink.core.TextUtil;

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

    public String info() {
        return TextUtil.colorize("&7[&b&l!&7] &r" + string);
    }

    public String success() {
        return TextUtil.colorize("&7[&a\u2714&7] &r" + string);
    }

    public String error() {
        return TextUtil.colorize("&7[&c\u2717&7] &r" + string);
    }

    public String toString() {
        return TextUtil.colorize("&7[&9&lD&2&lL&7] &r" + string);
    }
}