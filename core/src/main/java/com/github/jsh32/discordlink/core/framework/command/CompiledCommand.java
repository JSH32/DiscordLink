package com.github.jsh32.discordlink.core.framework.command;

import com.github.jsh32.discordlink.core.framework.command.annotation.Choice;
import com.github.jsh32.discordlink.core.framework.command.annotation.Command;
import com.github.jsh32.discordlink.core.framework.command.annotation.Default;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a compiled command.
 * TODO: Allow infinite subcommand nesting.
 */
public class CompiledCommand {
    private final boolean userOnly;
    private final CommandData baseCommand;
    private final Set<CommandData> subCommands;

    /**
     * Constructs a new CompiledCommand object.
     * @param command The command to compile.
     * @throws CommandCompileException If there is an error compiling the command.
     */
    public CompiledCommand(Object command) throws CommandCompileException {
        Command commandAnnotation = command.getClass().getAnnotation(Command.class);
        if (commandAnnotation == null)
            throw new CommandCompileException("The command must be annotated with the Command annotation");

        this.userOnly = commandAnnotation.userOnly();

        Collection<Method> defaultHandlers = Arrays.stream(command.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Default.class))
                .collect(Collectors.toUnmodifiableSet());

        if (defaultHandlers.size() != 1)
            throw new CommandCompileException("Each command must have exactly one Default handler");

        Method baseMethod = defaultHandlers.iterator().next();
        baseCommand = new CommandData(command, baseMethod, true, getArguments(baseMethod), baseMethod.getAnnotation(Default.class).userOnly());

        subCommands = Arrays.stream(command.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Command.class))
                .map(m -> new CommandData(command, m, false, getArguments(m), m.getAnnotation(Command.class).userOnly()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<ArgumentData> getArguments(Method method) {
        return Arrays.stream(method.getParameters())
                .skip(1)
                .map(parameter -> new ArgumentData(parameter.getType(), parameter.getName(),
                        parameter.isAnnotationPresent(Choice.class) ? parameter.getAnnotation(Choice.class).value() : null))
                .collect(Collectors.toList());
    }

    public boolean isUserOnly() {
        return userOnly;
    }

    public CommandData getBaseCommand() {
        return baseCommand;
    }

    public Set<CommandData> getSubCommands() {
        return subCommands;
    }
}
