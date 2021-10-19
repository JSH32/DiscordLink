package com.github.riku32.discordlink.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class SkinUtil {
    /**
     * Get an isometric head image
     *
     * @param uuid of the player
     * @return isometric head image
     */
    public static String getIsometricHeadStream(UUID uuid) {
        // Visage is currently not working
        //return new URL("https://visage.surgeplay.com/head/256/" + uuid.toString()).openStream();
        return String.format("https://mc-heads.net/head/%s/256.png", uuid.toString());
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
