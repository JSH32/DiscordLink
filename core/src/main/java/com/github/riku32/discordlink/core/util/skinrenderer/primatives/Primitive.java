package com.github.riku32.discordlink.core.util.skinrenderer.primatives;

import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.util.skinrenderer.Errors;
import com.github.riku32.discordlink.core.util.skinrenderer.renderers.Renderer;
import com.github.riku32.discordlink.core.util.skinrenderer.TextureType;

import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glUseProgram;

public abstract class Primitive {
    public float scaleX = 1.0f;
    public float scaleY = 1.0f;
    public float scaleZ = 1.0f;
    public float x, y, z, rotX, rotY, rotZ;
    public float anchorX, anchorY, anchorZ;
    public boolean lit = true;

    public boolean textured = true;
    public TextureType texture = TextureType.NONE;

    protected boolean inStage = true;

    public boolean depthMask = true;

    public abstract void render(Renderer renderer);

    protected void doRender(Renderer renderer, int vbo, int tcbo, float[] vertices) {
        glPushMatrix();
        glDepthMask(depthMask);
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Rendering "+getClass().getSimpleName());
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Translating to "+x+", "+y+", "+z);
        glTranslatef(x, y, z);
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Rotating by "+rotX+"°, "+rotY+"°, "+rotZ+"°, anchored at "+anchorX+", "+anchorY+", "+anchorZ);
        glTranslatef(anchorX, anchorY, anchorZ);
        glRotatef(rotX, 1, 0, 0);
        glRotatef(rotY, 0, 1, 0);
        glRotatef(rotZ, 0, 0, 1);
        glTranslatef(-anchorX, -anchorY, -anchorZ);
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Scaling by "+scaleX+"x, "+scaleY+"x, "+scaleZ+"x");
        glScalef(scaleX, scaleY*-1, scaleZ);

        if (!inStage && lit) {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Enabling lighting");
            glEnable(GL_LIGHTING);
        } else if (!inStage) {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Disabling lighting");
            glDisable(GL_LIGHTING);
        }
        if (textured) {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.log(Level.FINEST, "Enabling texturing - texture "+texture);
            glEnable(GL_TEXTURE_2D);
            if (texture == TextureType.ALL) {
                glBindTexture(GL_TEXTURE_2D, renderer.owner.shadowTexture);
                glUseProgram(0);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            } else {
                glBindTexture(GL_TEXTURE_2D, renderer.owner.skinFboTex);
                glUseProgram(renderer.owner.textureFilterProgram);
                glEnable(GL_BLEND);
                glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            }
            glDisable(GL_ALPHA_TEST);
        } else {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.log(Level.FINEST, "Disabling texturing");
            glDisable(GL_TEXTURE_2D);
            glUseProgram(0);
        }
        Errors.checkGLError();

        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Setting VBO");
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        if (tcbo == Integer.MAX_VALUE) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glTexCoordPointer(2, GL_FLOAT, 20, 12);
            glVertexPointer(3, GL_FLOAT, 20, 0);
        } else {
            glEnableClientState(GL_NORMAL_ARRAY);
            glBindBuffer(GL_ARRAY_BUFFER, tcbo);
            glTexCoordPointer(2, GL_FLOAT, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glVertexPointer(3, GL_FLOAT, 24, 0);
            glNormalPointer(GL_FLOAT, 24, 12);
        }

        Errors.checkGLError();

        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Rendering");
        if (tcbo == Integer.MAX_VALUE)
            glDrawArrays(GL_QUADS, 0, vertices.length/5);
        else
            glDrawArrays(GL_QUADS, 0, vertices.length/6);

        Errors.checkGLError();

        glDepthMask(true);
        glPopMatrix();
    }
}