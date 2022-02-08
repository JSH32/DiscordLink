package com.github.riku32.discordlink.core.util.skinrenderer;

public enum RenderType {
    FACE,
    FRONT,
    FRONT_FULL,

    HEAD,
    BUST,
    FULL,
    SKIN;

    public boolean isTall() {
        switch (this) {
            case FULL:
            case FRONT_FULL:
                return true;
            default:
                return false;
        }
    }

    public boolean is3D() {
        switch (this) {
            case HEAD:
            case FULL:
            case BUST:
                return true;
            default:
                return false;
        }
    }
}