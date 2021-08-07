package com.github.riku32.discordlink.core.platform.command.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Command annotation must be applied to a class to be used as a parent command.
 * <p>
 * The default base handler must be annotated with {@link Default}
 * <p>
 * This annotation can also be applied to child methods of the applied class, this only supports ONE level of nesting
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String[] aliases();
    String permission() default "";
}
