package com.github.riku32.discordlink.core.util.skinrenderer;

import com.github.riku32.discordlink.core.util.skinrenderer.renderers.*;

import java.awt.image.BufferedImage;

public final class RenderConfiguration {
    private RenderType mode;
    private boolean slim;
    private boolean flip;

    private final BufferedImage skinTexture;

    public RenderConfiguration(RenderType mode, BufferedImage skinTexture, boolean slim) {
        if (mode == null)
            throw new IllegalArgumentException("type cannot be null");

        this.mode = mode;
        this.slim = slim;
        this.skinTexture = skinTexture;
    }

    public Renderer createRenderer(SkinRenderer owner) {
        Renderer renderer;

        switch (mode) {
            case FACE:
                renderer = new FaceRenderer(owner);
                break;
            case FRONT:
            case FRONT_FULL:
                renderer = new FlatBodyRenderer(owner);
                break;
            case BUST:
            case FULL:
                renderer = new BodyRenderer(owner);
                break;
            case HEAD:
                renderer = new HeadRenderer(owner);
                break;
            default:
                throw new AssertionError("Missing mapping for " + mode);
        }

        renderer.init(slim, mode.isTall(), flip);
        return renderer;
    }

    public RenderType getMode() {
        return mode;
    }

    public void setType(RenderType mode) {
        if (mode == null)
            throw new IllegalArgumentException("Type cannot be null");

        this.mode = mode;
    }

    public boolean isSlim() {
        if (mode == RenderType.HEAD || mode == RenderType.FACE)
            return false;

        return slim;
    }

    public void setSlim(boolean slim) {
        this.slim = slim;
    }

    public boolean isFull() {
        if (mode == RenderType.HEAD || mode == RenderType.FACE)
            return false;

        return mode.isTall();
    }

    public BufferedImage getSkinTexture() {
        return skinTexture;
    }

    public void setFlipped(boolean flip) {
        this.flip = flip;
    }
}
