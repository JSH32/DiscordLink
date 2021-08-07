package com.github.riku32.discordlink.core.platform.command;

public class ArgumentData {
    private final Class<?> argumentType;
    private final String argumentName;
    private final String[] choices;

    public ArgumentData(Class<?> argumentType, String argumentName, String[] choices) {
        this.argumentType = argumentType;
        this.argumentName = argumentName;
        this.choices = choices;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public Class<?> getArgumentType() {
        return argumentType;
    }

    public String[] getChoices() {
        return choices;
    }
}
