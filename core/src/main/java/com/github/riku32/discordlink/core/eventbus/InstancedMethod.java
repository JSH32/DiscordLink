package com.github.riku32.discordlink.core.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An executable method with an instance attached
 */
public class InstancedMethod {
    private final Object object;
    private final Method method;

    public InstancedMethod(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    public Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(object, args);
    }

    public Object getObject() {
        return object;
    }

    public Method getMethod() {
        return method;
    }
}
