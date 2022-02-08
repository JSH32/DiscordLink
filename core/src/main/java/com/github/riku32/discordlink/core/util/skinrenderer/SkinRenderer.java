package com.github.riku32.discordlink.core.util.skinrenderer;

import com.github.riku32.discordlink.core.DiscordLink;
import com.github.riku32.discordlink.core.util.skinrenderer.renderers.Renderer;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.github.riku32.discordlink.core.util.skinrenderer.Errors.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SkinRenderer extends Thread {
    public static final float[] vertices = {
            // Front
            -1.0f, -1.0f,  1.0f,
            0, 0, 1,
            1.0f, -1.0f,  1.0f,
            0, 0, 1,
            1.0f,  1.0f,  1.0f,
            0, 0, 1,
            -1.0f,  1.0f,  1.0f,
            0, 0, 1,
            // Back
            -1.0f, -1.0f, -1.0f,
            0, 0, -1,
            1.0f, -1.0f, -1.0f,
            0, 0, -1,
            1.0f,  1.0f, -1.0f,
            0, 0, -1,
            -1.0f,  1.0f, -1.0f,
            0, 0, -1,
            // Top
            -1.0f,  1.0f,  1.0f,
            0, 1, 0,
            1.0f,  1.0f,  1.0f,
            0, 1, 0,
            1.0f,  1.0f, -1.0f,
            0, 1, 0,
            -1.0f,  1.0f, -1.0f,
            0, 1, 0,
            // Bottom
            -1.0f, -1.0f, -1.0f,
            0, -1, 0,
            1.0f, -1.0f, -1.0f,
            0, -1, 0,
            1.0f, -1.0f,  1.0f,
            0, -1, 0,
            -1.0f, -1.0f,  1.0f,
            0, -1, 0,
            // Left
            1.0f, -1.0f,  1.0f,
            1, 0, 0,
            1.0f, -1.0f, -1.0f,
            1, 0, 0,
            1.0f,  1.0f, -1.0f,
            1, 0, 0,
            1.0f,  1.0f,  1.0f,
            1, 0, 0,
            // Right
            -1.0f, -1.0f, -1.0f,
            -1, 0, 0,
            -1.0f, -1.0f,  1.0f,
            -1, 0, 0,
            -1.0f,  1.0f,  1.0f,
            -1, 0, 0,
            -1.0f,  1.0f, -1.0f,
            -1, 0, 0,
    };

    public static final float[] planeVertices = {
            -1.0f,  0.0f,  1.0f,
            0, 1, 0,
            1.0f,  0.0f,  1.0f,
            0, 1, 0,
            1.0f,  0.0f, -1.0f,
            0, 1, 0,
            -1.0f,  0.0f, -1.0f,
            0, 1, 0,
    };

    private static final int CANVAS_WIDTH = 512;
    private static final int CANVAS_HEIGHT = 832;

    private final BufferedImage shadow;
    private final BufferedImage skinUnderlay;
    private final String textureFilterVs, textureFilterFs;

    private static class RenderTask {
        public final RenderConfiguration renderConfiguration;
        public final CompletableFuture<byte[]> future;
        public final int height, width;

        public RenderTask(RenderConfiguration renderConfiguration, CompletableFuture<byte[]> future, int height, int width) {
            this.renderConfiguration = renderConfiguration;
            this.future = future;
            this.height = height;
            this.width = width;
        }
    }

    // Queue of tasks that need to be processed
    private final BlockingQueue<RenderTask> queue = new LinkedBlockingQueue<>();

    public int cubeVbo, planeVbo, skinTexture, shadowTexture, skinUnderlayTexture;

    private int fbo, swapFbo, swapFboTex;
    public int skinFbo, skinFboTex;
    public int textureFilterProgram;

    private boolean run = true;
    private static int threadNum = 0;

    public SkinRenderer(ClassLoader resourceClassLoader) {
        super(String.format("DiscordLink skin render thread (#%s)", threadNum++));

        try {
            shadow = ImageIO.read(Objects.requireNonNull(resourceClassLoader.getResource("renderer/texture/shadow.png")));
            skinUnderlay = ImageIO.read(Objects.requireNonNull(resourceClassLoader.getResource("renderer/texture/skin_underlay.png")));
            textureFilterVs = Resources.toString(Objects.requireNonNull(resourceClassLoader.getResource("renderer/shader/texturefilter.vs")), Charsets.UTF_8);
            textureFilterFs = Resources.toString(Objects.requireNonNull(resourceClassLoader.getResource("renderer/shader/texturefilter.fs")), Charsets.UTF_8);

            shadow.coerceData(true);
            skinUnderlay.coerceData(true);
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

    public CompletableFuture<byte[]> queueRenderTask(RenderConfiguration renderConfiguration, int height, int width) throws InterruptedException {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        queue.put(new RenderTask(renderConfiguration, future, height, width));
        return future;
    }

    @Override
    public void run() {
        try {
            DiscordLink.LOGGER.info("Initializing skin renderer");

            if (!glfwInit()) {
                checkGLFWError();
                throw new RuntimeException("Failed to initialize GLFW");
            }

            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);

            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

            // Make window invisible
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);

            long window = glfwCreateWindow(CANVAS_WIDTH, CANVAS_HEIGHT, "DiscordLink Skin Renderer", NULL, NULL);
            if (window == NULL) {
                checkGLFWError();
                throw new RuntimeException("Failed to create window");
            }

            glfwMakeContextCurrent(window);
            GL.createCapabilities();

            if (!GL.getCapabilities().OpenGL30)
                throw new RuntimeException("OpenGL 3.0 is required");

            if (!GL.getCapabilities().GL_ARB_texture_multisample)
                throw new RuntimeException("ARB_texture_multisample is required");

            // Setup debug messages if debug mode
            if (DiscordLink.DEBUG_MODE)
                GLUtil.setupDebugMessageCallback();

            IntBuffer ids = BufferUtils.createIntBuffer(2);
            glGenBuffers(ids);
            cubeVbo = ids.get();
            planeVbo = ids.get();
            checkGLError();

            IntBuffer textures = BufferUtils.createIntBuffer(5);
            glGenTextures(textures);
            skinTexture = textures.get();
            shadowTexture = textures.get();
            skinFboTex = textures.get();
            skinUnderlayTexture = textures.get();
            swapFboTex = textures.get();
            checkGLError();

            glBindTexture(GL_TEXTURE_2D, skinTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            checkGLError();

            TextureUtils.upload(shadow, GL_RGBA8, shadowTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            checkGLError();

            TextureUtils.upload(skinUnderlay, GL_RGBA8, skinUnderlayTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            checkGLError();

            glBindTexture(GL_TEXTURE_2D, skinFboTex);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 64, 64, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            checkGLError();

            glBindTexture(GL_TEXTURE_2D, swapFboTex);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, CANVAS_WIDTH, CANVAS_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            checkGLError();

            fbo = glGenFramebuffers();

            int depth = glGenRenderbuffers();
            int color = glGenRenderbuffers();

            glBindFramebuffer(GL_FRAMEBUFFER, fbo);

            glBindRenderbuffer(GL_RENDERBUFFER, depth);
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, 8, GL_DEPTH_COMPONENT24, CANVAS_WIDTH, CANVAS_HEIGHT);

            glBindRenderbuffer(GL_RENDERBUFFER, color);
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, 8, GL_RGBA8, CANVAS_WIDTH, CANVAS_HEIGHT);

            glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depth);
            glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, color);
            checkFramebufferStatus();


            skinFbo = glGenFramebuffers();

            glBindFramebuffer(GL_FRAMEBUFFER, skinFbo);
            glDrawBuffer(GL_COLOR_ATTACHMENT0);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, skinFboTex, 0);
            checkFramebufferStatus();

            glBindFramebuffer(GL_FRAMEBUFFER, 0);


            swapFbo = glGenFramebuffers();

            glBindFramebuffer(GL_FRAMEBUFFER, swapFbo);
            glDrawBuffer(GL_COLOR_ATTACHMENT0);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, swapFboTex, 0);
            checkFramebufferStatus();

            glBindFramebuffer(GL_FRAMEBUFFER, 0);


            textureFilterProgram = glCreateProgram();

            int textureFilterVS = glCreateShader(GL_VERTEX_SHADER);
            int textureFilterFS = glCreateShader(GL_FRAGMENT_SHADER);

            glShaderSource(textureFilterVS, textureFilterVs);
            glShaderSource(textureFilterFS, textureFilterFs);

            glCompileShader(textureFilterVS);
            glCompileShader(textureFilterFS);

            glAttachShader(textureFilterProgram, textureFilterVS);
            glAttachShader(textureFilterProgram, textureFilterFS);

            glLinkProgram(textureFilterProgram);
            checkGLError();

            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
            vertexBuffer.put(vertices);
            vertexBuffer.flip();
            glBindBuffer(GL_ARRAY_BUFFER, cubeVbo);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
            checkGLError();

            FloatBuffer planeVertexBuffer = BufferUtils.createFloatBuffer(planeVertices.length);
            planeVertexBuffer.put(planeVertices);
            planeVertexBuffer.flip();
            glBindBuffer(GL_ARRAY_BUFFER, planeVbo);
            glBufferData(GL_ARRAY_BUFFER, planeVertexBuffer, GL_STATIC_DRAW);
            checkGLError();

            glClearColor(0, 0, 0, 0);
            glClearDepth(1.0);
            checkGLError();

            glShadeModel(GL_SMOOTH);
            glCullFace(GL_BACK);
            checkGLError();

            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            checkGLError();

            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            checkGLError();

            FloatBuffer lightColor = BufferUtils.createFloatBuffer(4);
            lightColor.put(3f);
            lightColor.put(3f);
            lightColor.put(3f);
            lightColor.put(1.0f);
            lightColor.flip();
            glLightfv(GL_LIGHT0, GL_AMBIENT, lightColor);

            FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
            lightPosition.put(-4f);
            lightPosition.put(-2f);
            lightPosition.put(1f);
            lightPosition.put(1000f);
            lightPosition.flip();
            glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);

            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT0);
            glEnable(GL_RESCALE_NORMAL);
            glFrontFace(GL_CW);
            glShadeModel(GL_SMOOTH);
            checkGLError();

            DiscordLink.LOGGER.info("Skin renderer started");
            try {
                while (run) {
                    RenderTask task = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (task == null) continue;

                    try {
                        task.future.complete(process(task.renderConfiguration, task.width, task.height));
                    } catch (Exception e) {
                        DiscordLink.LOGGER.log(Level.SEVERE, "An unexpected error occurred while rendering", e);
                        ByteArrayOutputStream ex = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(ex);
                        oos.writeObject(e);
                        oos.flush();
                    }
                }
                glfwDestroyWindow(window);
            } catch (Exception e) {
                DiscordLink.LOGGER.log(Level.SEVERE, "A fatal error has occurred in the skin render thread run loop.", e);
            }
        } catch (Exception e) {
            DiscordLink.LOGGER.log(Level.SEVERE, "A fatal error has occurred while setting up skin render thread.", e);
        }
    }

    private byte[] process(RenderConfiguration renderConfiguration, int width, int height) throws Exception {
       // Visage.log.info("Received a job to render a "+width+"x"+height+" "+mode.name().toLowerCase()+" for "+(profile == null ? "null" : profile.getName()));
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        return draw(renderConfiguration, width, height);
    }

    private byte[] draw(RenderConfiguration conf, int width, int height) throws Exception {
        BufferedImage out;
        int color = conf.getSkinTexture().getRGB(32, 8);
        boolean equal = true;
        for (int x = 32; x < 64; x++) {
            for (int y = 0; y < 16; y++) {
                if (x < 40 && y < 8) continue;
                if (x > 54 && y < 8) continue;
                if (conf.getSkinTexture().getRGB(x, y) != color) {
                    equal = false;
                    break;
                }
            }
        }

        if (equal) {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Skin has solid colored helm, stripping");
            conf.getSkinTexture().setRGB(32, 0, 32, 16, new int[32*64], 0, 32);
        }

        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Got skin");
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest(conf.toString());

        Renderer renderer = conf.createRenderer(this);
        try {
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Uploading");
            TextureUtils.upload(conf.getSkinTexture(), GL_RGBA8, skinTexture);
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Rendering");

            glUseProgram(0);

            glBindFramebuffer(GL_FRAMEBUFFER, skinFbo);
            glClearColor(0, 0, 0, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glViewport(0, 0, 64, 64);
            glOrtho(0, 64, 0, 64, -1, 1);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            glEnable(GL_TEXTURE_2D);

            glDisable(GL_LIGHTING);
            glColor3f(1, 1, 1);
            glDisable(GL_ALPHA_TEST);
            glDisable(GL_CULL_FACE);

            glBindTexture(GL_TEXTURE_2D, skinUnderlayTexture);
            drawQuad(0, 0, 64, 64);

            glBindTexture(GL_TEXTURE_2D, skinTexture);
            if (conf.getSkinTexture().getHeight() == 32) {
                drawQuad(0, 0, 64, 32);
                drawFlippedLimb(16, 48, 0, 16);
                drawFlippedLimb(32, 48, 40, 16);
            } else {
                drawQuad(0, 0, 64, 64);
            }

            glBindFramebuffer(GL_FRAMEBUFFER, fbo);

            glClearColor(0, 0, 0, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderer.render(width, height);

            glUseProgram(0);

            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Rendered - reading pixels");
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, swapFbo);
            glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
            glBlitFramebuffer(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST);

            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            glDisable(GL_LIGHTING);
            glColor3f(1, 1, 1);
            glDisable(GL_ALPHA_TEST);
            glDisable(GL_CULL_FACE);
            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, 1, 0, 1, -10, 10);
            glViewport(0, 0, 1, 1);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glViewport(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            glBindTexture(GL_TEXTURE_2D, swapFboTex);
            drawQuad(0, 0, 1, 1);

            out = renderer.readPixels(width, height);
        } finally {
            renderer.finish();
            renderer.destroy();
            if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Finished renderer");
        }

        if (out == null) return null;
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(out, "PNG", png);
        if (DiscordLink.DEBUG_MODE) DiscordLink.LOGGER.finest("Wrote png");
        return png.toByteArray();
    }

    private void drawFlippedLimb(int x, int y, int u, int v) {
        drawFlippedSkinQuad(x+4, y+4, u+4, v+4, 4, 12);
        drawFlippedSkinQuad(x+12, y+4, u+12, v+4, 4, 12);

        drawFlippedSkinQuad(x+4, y, u+4, v, 4, 4);
        drawFlippedSkinQuad(x+8, y, u+8, v, 4, 4);

        drawSkinQuad(x, y+4, u+8, v+4, 4, 12);
        drawSkinQuad(x+8, y+4, u, v+4, 4, 12);
    }

    private void drawFlippedSkinQuad(int x, int y, int u, int v, int w, int h) {
        drawQuad(x, y, x+w, y+h, (u+w)/64f, (v)/32f, (u)/64f, (v+h)/32f);
    }

    private void drawSkinQuad(int x, int y, int u, int v, int w, int h) {
        drawQuad(x, y, x+w, y+h, (u)/64f, (v)/32f, (u+w)/64f, (v+h)/32f);
    }

    private void drawQuad(float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2) {
        glBegin(GL_QUADS);
        glTexCoord2f(u1, v1);
        glVertex2f(x1, y1);
        glTexCoord2f(u2, v1);
        glVertex2f(x2, y1);
        glTexCoord2f(u2, v2);
        glVertex2f(x2, y2);
        glTexCoord2f(u1, v2);
        glVertex2f(x1, y2);
        glEnd();
    }

    private void drawQuad(float x1, float y1, float x2, float y2) {
        drawQuad(x1, y1, x2, y2, 0, 0, 1, 1);
    }

    public void finish() {
        run = false;
    }
}
