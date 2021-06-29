package com.github.riku32.discordlink;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;

/**
 * Random basic utility classes
 */
public final class Util {
    /**
     * Transform color to a color code string representation
     * This does not need to be used with {@link net.md_5.bungee.api.ChatColor#translateAlternateColorCodes(char, String)} as it automatically uses the color rune
     *
     * @param color to convert
     * @return color string
     */
    public static String colorToChatString(Color color) {
        String hexColor = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        String newString = ChatColor.COLOR_CHAR + "x";

        for (int i = 0; i < hexColor.length(); i++)
            newString = newString.concat(String.valueOf(ChatColor.COLOR_CHAR) + hexColor.charAt(i));

        return newString;
    }
}
