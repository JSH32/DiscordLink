package com.github.riku32.discordlink.core.framework.eventbus;

import com.github.riku32.discordlink.core.framework.eventbus.annotation.EventHandler;
import com.github.riku32.discordlink.core.framework.eventbus.events.Event;
import com.github.riku32.discordlink.core.framework.eventbus.exceptions.EventPostException;
import com.github.riku32.discordlink.core.framework.eventbus.exceptions.ListenerRegisterException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Add priority to event listeners
public class EventBus {
    private final Map<Class<?>, Set<InstancedMethod>> subscribers = new IdentityHashMap<>();

    public <T extends Event> void post(T event) throws EventPostException {
        Set<InstancedMethod> methods = subscribers.keySet().stream()
                .filter(key -> key.isAssignableFrom(event.getClass()))
                .map(subscribers::get)
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());

        for (InstancedMethod method : methods) {
            if (event.isCancelled()) return;
            try {
                method.invoke(event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new EventPostException(String.format("Unable to call handler %s on %s\nError: %s",
                        method.getMethod().getName(),
                        method.getObject().getClass().getName(),
                        ExceptionUtils.getStackTrace(e)));
            }
        }
    }

    public void register(Object object) throws ListenerRegisterException {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;

            if (method.getParameterTypes().length > 1) {
                throw new ListenerRegisterException(String.format("Unable to register listener %s because method %s contained more than one parameter",
                        object.getClass().getName(), method.getName()));
            }

            if (method.getParameterTypes().length < 1) {
                throw new ListenerRegisterException(String.format("Unable to register listener %s because method %s contained no parameters",
                        object.getClass().getName(), method.getName()));
            }

            if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                throw new ListenerRegisterException(String.format("Unable to register listener %s because method %s's parameter was not an Event",
                        object.getClass().getName(), method.getName()));
            }

            method.setAccessible(true);
            subscribers.computeIfAbsent(method.getParameterTypes()[0], k -> new HashSet<>())
                .add(new InstancedMethod(object, method));
        }
    }

    /**
     * Remove all handlers for an event
     *
     * @param event to remove from the eventbus
     */
    public <T extends Event> void clearEvent(Class<T> event) {
        subscribers.remove(event);
    }

    /**
     * Clear the eventbus and all handlers
     */
    public void clear() {
        subscribers.clear();
    }

    /**
     * Remove a listener from the event bus
     */
    public void removeListener(Object listener) {
        subscribers.values().forEach(m -> m.removeIf(method -> method.getObject().equals(listener)));
    }
}
