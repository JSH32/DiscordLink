package com.github.riku32.discordlink.core.framework.dependency.exceptions;

import java.lang.reflect.Field;

public class DependencyNotNullException extends Exception {
    public DependencyNotNullException(Field field) {
        super(String.format("Field %s on class %s must be null in order to inject", field.getName(), field.getDeclaringClass().getName()));
    }
}