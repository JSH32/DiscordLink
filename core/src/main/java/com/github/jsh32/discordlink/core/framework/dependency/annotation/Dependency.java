package com.github.jsh32.discordlink.core.framework.dependency.annotation;

import java.lang.annotation.*;

/**
 * Inject a dependency to a field of a command or event listener when passed into the framework
 * <p>
 * Parameters can be named as well
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Dependency {
    String named() default "";
}
