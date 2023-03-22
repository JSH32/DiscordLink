package com.github.riku32.discordlink.core.framework.command.annotation;

import java.lang.annotation.*;

/**
 * Apply to the default command in a command object
 * <p>
 * Every command annotated class <b>MUST</b> have at least one method annotated with this
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Default {
    boolean userOnly() default false;
}
