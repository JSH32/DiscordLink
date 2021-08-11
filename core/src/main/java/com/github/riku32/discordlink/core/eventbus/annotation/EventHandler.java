package com.github.riku32.discordlink.core.eventbus.annotation;

import java.lang.annotation.*;

/**
 * Apply to a method with the event's type being the first and only argument
 * <p>
 * The parent class may be registered into {@link com.github.riku32.discordlink.core.eventbus.EventBus}
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {}
