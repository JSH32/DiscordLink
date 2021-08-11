package com.github.riku32.discordlink.core;

public final class TextUtil {
    private static final char COLOR_CHAR = '\u00A7';

    /**
     * Colorize a string with Minecraft color char
     *
     * @param string to colorize
     * @return colorized string
     */
    public static String colorize(String string) {
        return string.replace('&', COLOR_CHAR);
    }
}
