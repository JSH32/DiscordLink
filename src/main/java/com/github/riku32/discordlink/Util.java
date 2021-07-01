package com.github.riku32.discordlink;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

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

    /**
     * Get an isometric head image from visage API
     * This must be downloaded due to visage not being able to embed images in discord
     *
     * @param uuid of the player
     * @return isometric head image
     */
    public static InputStream getIsometricHeadStream(UUID uuid) throws IOException {
        return new URL("https://visage.surgeplay.com/head/256/" + uuid.toString()).openStream();
    }

    /**
     * Get a front-facing head URL, this uses mc-heads.net
     * URL can be embedded in discord, this is used instead of visage since a URL is needed and not a file
     *
     * @param uuid of the player
     * @return front-facing head image
     */
    public static String getHeadURL(UUID uuid) {
        return String.format("https://mc-heads.net/avatar/%s/128.png", uuid.toString());
    }
}
