package com.github.jsh32.discordlink.core.framework.command;

import com.github.jsh32.discordlink.core.framework.PlatformPlayer;
import com.github.jsh32.discordlink.core.framework.command.annotation.Choice;
import com.github.jsh32.discordlink.core.framework.command.annotation.Command;
import net.dv8tion.jda.api.entities.Member;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * This represents the data for a single command.
 */
public class CommandData {
    private final Object instance;
    private final Method method;
    private final Command commandAnnotation;
    private final List<ArgumentData> argumentData;
    private final boolean userOnly;

    private final static List<Class<?>> ALLOWED_ARGS = Arrays.asList(
            String.class,
            boolean.class,
            PlatformPlayer.class,
            Member.class
    );

    /**
     * Constructs a new CommandData object.
     * @param instance The instance of the command class.
     * @param method The method that handles the command.
     * @param mainCommand Whether this is the main command or a subcommand.
     * @param argumentData The arguments of the command.
     * @param userOnly Whether the command is only for users.
     * @throws CommandCompileException If there is an error compiling the command.
     */
    public CommandData(Object instance, Method method, boolean mainCommand, List<ArgumentData> argumentData, boolean userOnly)
            throws CommandCompileException {
        this.instance = instance;
        this.method = method;
        this.argumentData = argumentData;
        this.userOnly = userOnly;

        method.setAccessible(true);

        Parameter[] parameters = method.getParameters();

        if (!parameters[0].getType().equals(CommandSender.class))
            throw new CommandCompileException("Command handler's first argument must be a CommandSender");

        for (int i = 1; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (!ALLOWED_ARGS.contains(parameter.getType()))
                throw new CommandCompileException(String.format("Invalid argument type %s was provided", parameter.getType().getName()));

            if (parameter.getType() != String.class && parameter.isAnnotationPresent(Choice.class))
                throw new CommandCompileException("Only a String argument can be annotated with Choice");
        }

        if (mainCommand)
            commandAnnotation = instance.getClass().getAnnotation(Command.class);
        else
            commandAnnotation = method.getAnnotation(Command.class);
    }
    
    /**
     * Invokes the method that handles the command.
     * @param args The arguments to pass to the method.
     * @throws InvocationTargetException If the invoked method throws an exception.
     */
    public void invoke(Object... args) throws InvocationTargetException {
        try {
            method.invoke(instance, args);
        } catch (IllegalAccessException ignored) {}
    }

    public List<ArgumentData> getArguments() {
        return argumentData;
    }

    public String[] getAliases() {
        return commandAnnotation.aliases();
    }

    public String getPermission() {
        return commandAnnotation.permission();
    }

    public boolean isUserOnly() {
        return userOnly;
    }
}
