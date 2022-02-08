package com.github.riku32.discordlink.core.util.skinrenderer;

import com.github.riku32.discordlink.core.DiscordLink;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;

import static org.lwjgl.glfw.GLFW.GLFW_NO_ERROR;
import static org.lwjgl.glfw.GLFW.glfwGetError;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11C.glGetError;
import static org.lwjgl.opengl.GL30.*;

public class Errors {
    private static Multimap<Integer, String> mapping;

    private static void buildMapping() {
        if (mapping != null) return;
        Multimap<Integer, String> map = HashMultimap.create();
        List<Class<?>> classes = ImmutableList.of(
                GL11.class, GL12.class, GL13.class, GL14.class, GL15.class,
                GL20.class, GL21.class, GL30.class, GL31.class, GL32.class,
                GL33.class, GL40.class, GL41.class, GL42.class, GL43.class,
                GL44.class, GL45.class, GLFW.class
        );

        for (Class<?> clazz : classes) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getName().toUpperCase(Locale.ROOT).equals(f.getName()) &&
                        f.getType() == int.class && Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
                    List<String> li = Splitter.on('_').splitToList(f.getName());
                    li = li.subList(1, li.size());
                    String clean =
                            Joiner.on(' ').join(
                                    li.stream()
                                            .map(Errors::toTitleCase)
                                            .iterator());
                    try {
                        map.put(f.getInt(null), clean);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }

        mapping = map;
    }

    private static String toTitleCase(String str) {
        return str.charAt(0)+str.substring(1).toLowerCase(Locale.ROOT);
    }

    public static void checkGLError() {
        if (DiscordLink.DEBUG_MODE) {
            int err = glGetError();
            while (err != GL_NO_ERROR) {
                buildMapping();
                DiscordLink.LOGGER.warning("== GL ERROR ==");
                DiscordLink.LOGGER.warning("0x"+Integer.toHexString(err).toUpperCase(Locale.ROOT)+" ("+Joiner.on(", ").join(mapping.get(err))+")");
                for (StackTraceElement ste : new Throwable().fillInStackTrace().getStackTrace())
                    DiscordLink.LOGGER.warning(ste.toString());
                err = glGetError();
            }
        }
    }

    public static void checkGLFWError() {
        if (DiscordLink.DEBUG_MODE) {
            int err = glfwGetError(null);
            while (err != GLFW_NO_ERROR) {
                buildMapping();
                DiscordLink.LOGGER.warning("== GLFW ERROR ==");
                DiscordLink.LOGGER.warning("0x"+Integer.toHexString(err).toUpperCase(Locale.ROOT)+" ("+Joiner.on(", ").join(mapping.get(err))+")");
                for (StackTraceElement ste : new Throwable().fillInStackTrace().getStackTrace())
                    DiscordLink.LOGGER.warning(ste.toString());
                err = glfwGetError(null);
            }
        }
    }

    public static void checkFramebufferStatus() {
        if (DiscordLink.DEBUG_MODE) {
            int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
            buildMapping();
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                DiscordLink.LOGGER.warning("== FRAMEBUFFER INCOMPLETE ==");
                DiscordLink.LOGGER.warning("0x"+Integer.toHexString(status).toUpperCase(Locale.ROOT)+" ("+ Joiner.on(", ").join(mapping.get(status))+")");
                for (StackTraceElement ste : new Throwable().fillInStackTrace().getStackTrace())
                    DiscordLink.LOGGER.warning(ste.toString());
            }
        }
    }
}