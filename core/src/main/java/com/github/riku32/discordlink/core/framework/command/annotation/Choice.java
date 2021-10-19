package com.github.riku32.discordlink.core.framework.command.annotation;

import java.lang.annotation.*;

/**
 * Apply to a parameter of a command to indicate that the only value this argument can and will receive will be one of the choices
 * <p>
 * This may only be applied to {@link String} arguments
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Choice {
    String[] value();
}
