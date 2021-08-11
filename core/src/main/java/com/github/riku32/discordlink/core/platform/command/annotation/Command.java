package com.github.riku32.discordlink.core.platform.command.annotation;

import java.lang.annotation.*;

/**
 * Command annotation must be applied to a class to be used as a parent command.
 * <p>
 * The default base handler <b>MUST</b> be annotated with {@link Default}
 * <p>
 * This annotation can also be applied to child methods of the applied class, this only supports <b>ONE</b> level of nesting
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Command {
    String[] aliases();
    String permission() default "";
    boolean userOnly() default false;
}
