package com.github.riku32.discordlink.core.util.skinrenderer.renderers;

import com.github.riku32.discordlink.core.util.skinrenderer.SkinRenderer;
import com.github.riku32.discordlink.core.util.skinrenderer.TextureType;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Group;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Plane;

public class FaceRenderer extends Renderer {
    public FaceRenderer(SkinRenderer owner) {
        super(owner);
    }

    protected void initPrimitives(boolean slim, boolean full, boolean flip) {
        Group stage = new Group();
        stage.y = 0;
        stage.z = -2.5f;
        stage.rotZ = 0;
        stage.rotY = flip ? 180 : 0;
        stage.rotX = -90;
        stage.lit = false;
        addPrimitive(stage);

        Plane head = new Plane();
        head.y = 0;
        head.z = 0;
        head.texture = TextureType.HEAD_FRONT;
        stage.members.add(head);
        Plane helm = new Plane();
        helm.scaleX = helm.scaleY = helm.scaleZ = 1.05f;
        helm.texture = TextureType.HEAD2_FRONT;
        helm.depthMask = false;
        stage.members.add(helm);
    }
}