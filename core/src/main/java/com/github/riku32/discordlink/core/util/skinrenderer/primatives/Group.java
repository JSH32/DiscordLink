package com.github.riku32.discordlink.core.util.skinrenderer.primatives;

import com.github.riku32.discordlink.core.util.skinrenderer.renderers.Renderer;

import java.util.ArrayList;
import java.util.List;

import com.github.riku32.discordlink.core.DiscordLink;

import static org.lwjgl.opengl.GL11.*;

public class Group extends Primitive {
    public final List<Primitive> members = new ArrayList<>();

    public void render(Renderer renderer) {
        glPushMatrix();
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Rendering "+getClass().getSimpleName());
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Translating to "+x+", "+y+", "+z);
        glTranslatef(x, y, z);
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Rotating by "+rotX+"°, "+rotY+"°, "+rotZ+"°");
        glRotatef(rotX, 1.0f, 0.0f, 0.0f);
        glRotatef(rotY, 0.0f, 1.0f, 0.0f);
        glRotatef(rotZ, 0.0f, 0.0f, 1.0f);
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Scaling by "+scaleX+"x, "+scaleY+"x, "+scaleZ+"x");
        glScalef(scaleX, scaleY, scaleZ);

        if (lit) {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Enabling lighting");
            glEnable(GL_LIGHTING);
        } else {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Disabling lighting");
            glDisable(GL_LIGHTING);
        }

        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Rendering");
        for (Primitive p : members) {
            p.inStage = true;
            p.render(renderer);
        }
        glPopMatrix();
    }

}