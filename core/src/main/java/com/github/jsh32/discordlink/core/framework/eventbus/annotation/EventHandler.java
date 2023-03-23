package com.github.jsh32.discordlink.core.framework.eventbus.annotation;

import com.github.jsh32.discordlink.core.framework.eventbus.EventBus;

import java.lang.annotation.*;

/**
 * Apply to a method with the event's type being the first and only argument
 * <p>
 * The parent class may be registered into {@link EventBus}
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {}
