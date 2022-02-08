package com.github.riku32.discordlink.core.util.skinrenderer.primatives;

import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.util.skinrenderer.SkinRenderer;
import com.github.riku32.discordlink.core.util.skinrenderer.renderers.Renderer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

public class Cube extends Primitive {
    private int tcbo = Integer.MAX_VALUE;

    public void render(Renderer renderer) {
        if (tcbo == Integer.MAX_VALUE) {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Creating texture coord buffer");
            tcbo = glGenBuffers();
            FloatBuffer uv = BufferUtils.createFloatBuffer(texture.u.length+texture.v.length);
            for (int i = 0; i < texture.u.length; i++) {
                uv.put(texture.u[i]);
                uv.put(texture.v[i]);
            }
            uv.flip();
            glBindBuffer(GL_ARRAY_BUFFER, tcbo);
            glBufferData(GL_ARRAY_BUFFER, uv, GL_STATIC_DRAW);
        }
        doRender(renderer, renderer.owner.cubeVbo, tcbo, SkinRenderer.vertices);
    }
}