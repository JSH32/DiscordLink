package com.github.riku32.discordlink.core.framework.command;

import com.github.riku32.discordlink.core.framework.command.annotation.Command;

import java.lang.reflect.Method;
import java.util.List;

public class CommandData {
    private final Object instance;
    private final Method method;
    private final Command commandAnnotation;
    private final List<ArgumentData> argumentData;
    private final boolean userOnly;

    public CommandData(Object instance, Method method, boolean mainCommand, List<ArgumentData> argumentData, boolean userOnly) {
        this.instance = instance;
        this.method = method;
        this.argumentData = argumentData;
        this.userOnly = userOnly;

        method.setAccessible(true);

        if (mainCommand)
            commandAnnotation = instance.getClass().getAnnotation(Command.class);
        else
            commandAnnotation = method.getAnnotation(Command.class);
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }

    public List<ArgumentData> getArguments() {
        return argumentData;
    }

    public String[] getAliases() {
        return commandAnnotation.aliases();
    }

    public Command getAnnotation() {
        return commandAnnotation;
    }

    public boolean isUserOnly() {
        return userOnly;
    }
}
