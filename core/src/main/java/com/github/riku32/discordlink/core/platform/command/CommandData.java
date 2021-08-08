package com.github.riku32.discordlink.core.platform.command;

import com.github.riku32.discordlink.core.platform.command.annotation.Command;

import java.lang.reflect.Method;
import java.util.List;

public class CommandData {
    private final Object instance;
    private final Method method;
    private final Command commandAnnotation;
    private final List<ArgumentData> argumentData;

    public CommandData(Object instance, Method method, boolean mainCommand, List<ArgumentData> argumentData) {
        this.instance = instance;
        this.method = method;
        this.argumentData = argumentData;

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

    public String getPermission() {
        return commandAnnotation.permission().equals("") ? null : commandAnnotation.permission();
    }
}
