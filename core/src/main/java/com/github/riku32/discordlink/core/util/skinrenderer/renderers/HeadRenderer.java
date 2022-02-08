package com.github.riku32.discordlink.core.util.skinrenderer.renderers;

import com.github.riku32.discordlink.core.util.skinrenderer.SkinRenderer;
import com.github.riku32.discordlink.core.util.skinrenderer.TextureType;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Cube;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Group;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Plane;

public class HeadRenderer extends Renderer {
    public HeadRenderer(SkinRenderer owner) {
        super(owner);
    }

    protected void initPrimitives(boolean slim, boolean full, boolean flip) {
        float tilt = -20;
        float angle = -35;

        Group stage = new Group();
        stage.y = -0.25f;
        stage.z = -5f;
        stage.rotX = tilt;
        stage.rotY = angle;
        addPrimitive(stage);

        Plane shadow = new Plane();
        shadow.y = 1;
        shadow.scaleX = shadow.scaleZ = 1.95f;
        shadow.texture = TextureType.ALL;
        shadow.lit = false;
        stage.members.add(shadow);
        Cube head = new Cube();
        head.y = -0.025f;
        head.z = -0.025f;
        if (flip) head.rotZ = 180f;
        head.texture = TextureType.HEAD;
        stage.members.add(head);
        Cube helm = new Cube();
        helm.scaleX = helm.scaleY = helm.scaleZ = 1.05f;
        if (flip) helm.rotZ = 180f;
        helm.texture = TextureType.HEAD2;
        helm.depthMask = false;
        stage.members.add(helm);
    }
}