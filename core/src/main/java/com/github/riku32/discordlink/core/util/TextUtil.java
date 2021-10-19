package com.github.riku32.discordlink.core.util;

import java.awt.*;

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

    /**
     * Transform color to a color code string representation
     *
     * @param color to convert
     * @return color string
     */
    public static String colorToChatString(Color color) {
        String hexColor = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        String newString = COLOR_CHAR + "x";

        for (int i = 0; i < hexColor.length(); i++)
            newString = newString.concat(String.valueOf(COLOR_CHAR) + hexColor.charAt(i));

        return newString;
    }
}
