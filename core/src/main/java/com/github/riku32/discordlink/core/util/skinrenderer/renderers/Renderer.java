package com.github.riku32.discordlink.core.util.skinrenderer.renderers;

import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.util.skinrenderer.SkinRenderer;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Primitive;
import com.google.common.base.Joiner;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.github.riku32.discordlink.core.util.skinrenderer.Errors.checkGLError;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;

public abstract class Renderer {
    public final String name = getClass().getSimpleName();
    public List<Primitive> prims = new ArrayList<>();

    private boolean initialized = false;

    public SkinRenderer owner;

    public Renderer(SkinRenderer owner) {
        this.owner = owner;
    }

    protected void addPrimitive(Primitive prim) {
        prims.add(prim);
    }

    public void render(int width, int height) {
        initGL(width, height);

        for (Primitive prim : prims)
            prim.render(this);

        checkGLError();
    }

    public void destroy() {
        prims.clear();
        initialized = false;
    }

    public void init(boolean slim, boolean full, boolean flip) {
        if (DiscordLink.DEBUG_MODE) {
            List<String> modes = new ArrayList<>();
            if (slim) modes.add("slim");
            if (full) modes.add("full");
            if (flip) modes.add("flip");
            DiscordLink.LOGGER.finer("["+name+"] Initializing primitives"+(modes.isEmpty() ? "" : " ("+ Joiner.on(", ").join(modes)+")"));
        }
        initPrimitives(slim, full, flip);
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    protected abstract void initPrimitives(boolean slim, boolean full, boolean flip);
    protected void initGL(float width, float height) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glViewport(0, 0, (int)width, (int)height);
        glEnable(GL_DEPTH_TEST);

        double fov = 45;
        double aspect = width/height;

        double zNear = 0.1;
        double zFar = 100;

        double fH = Math.tan((fov / 360) * Math.PI) * zNear;
        double fW = fH * aspect;
        glFrustum(-fW, fW, -fH, fH, zNear, zFar);

        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glEnable(GL_CULL_FACE);
    }

    public void finish() {}

    public BufferedImage readPixels(int width, int height) {
        glReadBuffer(GL_FRONT);
        ByteBuffer buf = BufferUtils.createByteBuffer(width * height * 4);
        glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, buf);
        checkGLError();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width*height];
        buf.asIntBuffer().get(pixels);
        img.setRGB(0, 0, width, height, pixels, 0, width);
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Read pixels");
        return img;
    }
}