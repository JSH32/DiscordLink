package com.github.jsh32.discordlink.core.framework.command;

/**
 * This represents the data for a single command argument.
 */
public record ArgumentData(Class<?> argumentType, String argumentName, String[] choices) {
}
