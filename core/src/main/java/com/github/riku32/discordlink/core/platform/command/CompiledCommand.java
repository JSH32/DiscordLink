package com.github.riku32.discordlink.core.platform.command;

import com.github.riku32.discordlink.core.platform.command.annotation.Choice;
import com.github.riku32.discordlink.core.platform.command.annotation.Command;
import com.github.riku32.discordlink.core.platform.command.annotation.Default;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CompiledCommand {
    private final CommandData baseCommand;
    private final Set<CommandData> subCommands;

    public CompiledCommand(Object command) throws CommandCompileException {
        Collection<Method> defaultHandlers = Arrays.stream(command.getClass().getDeclaredMethods())
                .peek(m -> m.setAccessible(true))
                .filter(m -> m.isAnnotationPresent(Default.class))
                .collect(Collectors.toUnmodifiableSet());

        if (defaultHandlers.size() != 1)
            throw new CommandCompileException("Each command must have exactly one Default handler");

        Method baseMethod = defaultHandlers.iterator().next();
        baseCommand = new CommandData(command, baseMethod, true, getArgumentData(baseMethod));

        if (!baseMethod.getParameterTypes()[0].equals(CommandSender.class))
            throw new CommandCompileException("Command handler's first argument must be a CommandSender");

        subCommands = Arrays.stream(command.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Command.class))
                .map(m -> new CommandData(command, m, false, getArgumentData(m)))
                .collect(Collectors.toUnmodifiableSet());

        for (CommandData subCommand : subCommands) {
            if (!subCommand.getMethod().getParameterTypes()[0].equals(CommandSender.class))
                throw new CommandCompileException("Command handler's first argument must be a CommandSender");
        }
    }

    private List<ArgumentData> getArgumentData(Method method) {
        return Arrays.stream(method.getParameters())
                .skip(1)
                .map(parameter -> new ArgumentData(parameter.getType(), parameter.getName(),
                        parameter.isAnnotationPresent(Choice.class) ? parameter.getAnnotation(Choice.class).value() : null))
                .collect(Collectors.toUnmodifiableList());
    }

    public CommandData getBaseCommand() {
        return baseCommand;
    }

    public Set<CommandData> getSubCommands() {
        return subCommands;
    }
}
