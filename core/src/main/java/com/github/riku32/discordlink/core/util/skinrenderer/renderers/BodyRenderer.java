package com.github.riku32.discordlink.core.util.skinrenderer.renderers;

import com.github.riku32.discordlink.core.util.skinrenderer.SkinRenderer;
import com.github.riku32.discordlink.core.util.skinrenderer.TextureType;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Cube;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Group;
import com.github.riku32.discordlink.core.util.skinrenderer.primatives.Plane;

public class BodyRenderer extends Renderer {
    public BodyRenderer(SkinRenderer owner) {
        super(owner);
    }

    protected void initPrimitives(boolean slim, boolean full, boolean flip) {
        float tilt = -10;
        float angle = 20;

        Group group = new Group();
        group.x = 0;
        group.y = full ? (flip ? -2.7f : -2.8f) : -1f;
        group.z = full ? -10.35f : -6f;
        group.rotX = tilt;
        group.rotY = angle;
        addPrimitive(group);

        if (full || flip) {
            Plane shadow = new Plane();
            shadow.y = full ? (flip ? 6.825f : 7f) : 2.85f;
            shadow.scaleX = 1.85f;
            shadow.scaleZ = flip ? 1.85f : 0.85f;
            shadow.texture = TextureType.ALL;
            shadow.lit = false;
            group.members.add(shadow);
        }

        Group group2 = new Group();
        if (flip) {
            group2.rotZ = 180;
            group2.y = ((-group.y)*2)+(full ? 0.3f : -0.25f);
        }
        group.members.add(group2);

        Cube head = new Cube();
        head.texture = TextureType.HEAD;

        Cube head2 = new Cube();
        head2.scaleX = head2.scaleY = head2.scaleZ = 1.05f;
        head2.texture = TextureType.HEAD2;
        head2.depthMask = false;



        Cube body = new Cube();
        body.y = 2.5f;
        body.scaleY = 1.5f;
        body.scaleZ = 0.5f;
        body.texture = TextureType.BODY;

        Cube body2 = new Cube();
        body2.y = 2.5f;
        body2.scaleY = 1.55f;
        body2.scaleZ = 0.55f;
        body2.scaleX = 1.05f;
        body2.texture = TextureType.BODY2;
        body2.depthMask = false;



        Cube larm = new Cube();
        larm.x = slim ? 1.375f : 1.5f;
        larm.y = 2.5f;
        larm.scaleY = 1.5f;
        larm.scaleZ = 0.5f;
        larm.scaleX = slim ? 0.375f : 0.5f;
        larm.anchorX = -larm.scaleX;
        larm.anchorY = -larm.scaleY;
        larm.rotZ = -10f;
        larm.texture = slim ? TextureType.LARM_SLIM : TextureType.LARM;

        Cube larm2 = new Cube();
        larm2.x = slim ? 1.375f : 1.5f;
        larm2.y = 2.5f;
        larm2.scaleY = 1.55f;
        larm2.scaleZ = 0.55f;
        larm2.scaleX = slim ? 0.425f : 0.55f;
        larm2.anchorX = -larm2.scaleX;
        larm2.anchorY = -larm2.scaleY;
        larm2.rotZ = -10f;
        larm2.texture = slim ? TextureType.LARM2_SLIM : TextureType.LARM2;
        larm2.depthMask = false;


        Cube rarm = new Cube();
        rarm.x = slim ? -1.375f : -1.5f;
        rarm.y = 2.5f;
        rarm.scaleY = 1.5f;
        rarm.scaleZ = 0.5f;
        rarm.scaleX = slim ? 0.375f : 0.5f;
        rarm.anchorX = rarm.scaleX;
        rarm.anchorY = -rarm.scaleY;
        rarm.rotZ = 10f;
        rarm.texture = slim ? TextureType.RARM_SLIM : TextureType.RARM;

        Cube rarm2 = new Cube();
        rarm2.x = slim ? -1.375f : -1.5f;
        rarm2.y = 2.5f;
        rarm2.scaleY = 1.55f;
        rarm2.scaleZ = 0.55f;
        rarm2.scaleX = slim ? 0.425f : 0.55f;
        rarm2.anchorX = rarm2.scaleX;
        rarm2.anchorY = -rarm2.scaleY;
        rarm2.rotZ = 10f;
        rarm2.texture = slim ? TextureType.RARM2_SLIM : TextureType.RARM2;
        rarm2.depthMask = false;


        Cube lleg = new Cube();
        lleg.x = 0.5f;
        lleg.y = 5.5f;
        lleg.scaleY = 1.5f;
        lleg.scaleZ = 0.5f;
        lleg.scaleX = 0.5f;
        lleg.anchorY = -lleg.scaleY;
        lleg.texture = TextureType.LLEG;

        Cube lleg2 = new Cube();
        lleg2.x = 0.5f;
        lleg2.y = 5.5f;
        lleg2.scaleY = 1.55f;
        lleg2.scaleZ = 0.55f;
        lleg2.scaleX = 0.55f;
        lleg2.anchorY = -lleg2.scaleY;
        lleg2.texture = TextureType.LLEG2;
        lleg2.depthMask = false;


        Cube rleg = new Cube();
        rleg.x = -0.5f;
        rleg.y = 5.5f;
        rleg.scaleY = 1.5f;
        rleg.scaleZ = 0.5f;
        rleg.scaleX = 0.5f;
        rleg.anchorY = -rleg.scaleY;
        rleg.texture = TextureType.RLEG;

        Cube rleg2 = new Cube();
        rleg2.x = -0.5f;
        rleg2.y = 5.5f;
        rleg2.scaleY = 1.55f;
        rleg2.scaleZ = 0.55f;
        rleg2.scaleX = 0.55f;
        rleg2.anchorY = -rleg2.scaleY;
        rleg2.texture = TextureType.RLEG2;
        rleg2.depthMask = false;

        group2.members.add(head);
        group2.members.add(body);
        group2.members.add(larm);
        group2.members.add(rarm);
        group2.members.add(lleg);
        group2.members.add(rleg);

        group2.members.add(lleg2);
        group2.members.add(rleg2);
        group2.members.add(body2);
        group2.members.add(head2);
        group2.members.add(larm2);
        group2.members.add(rarm2);
    }
}