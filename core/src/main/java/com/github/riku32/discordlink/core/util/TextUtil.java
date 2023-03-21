package com.github.riku32.discordlink.core.util;

import java.awt.*;

public class TextUtil {
    public static String colorToHexMM(Color color) {
        return String.format("<#%s>", Integer.toHexString(color.getRGB() & 0xffffff));
    }
}
