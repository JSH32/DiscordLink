package com.github.riku32.discordlink.core.util.skinrenderer;

import static com.github.riku32.discordlink.core.util.skinrenderer.Errors.checkGLError;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import com.github.riku32.discordlink.core.DiscordLink;
import org.lwjgl.BufferUtils;

public class TextureUtils {
    public static void upload(BufferedImage img, int format, int tex) {
        int width = img.getWidth();
        int height = img.getHeight();
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Uploading "+width+"x"+height+" ("+(width*height)+" pixel) image");

        BufferedImage unIndexed = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] argb = new int[width*height];
        img.getRGB(0, 0, width, height, argb, 0, width);

        unIndexed.setRGB(0, 0, width, height, argb, 0, width);
        unIndexed.coerceData(true);
        unIndexed.getRGB(0, 0, width, height, argb, 0, width);

        IntBuffer buf = BufferUtils.createIntBuffer(width*height);
        buf.put(argb);
        buf.flip();

        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, GL_BGRA, GL_UNSIGNED_BYTE, buf);

        checkGLError();
    }
}
