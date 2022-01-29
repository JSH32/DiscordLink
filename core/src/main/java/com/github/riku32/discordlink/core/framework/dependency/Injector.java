package com.github.riku32.discordlink.core.framework.dependency;

import com.github.riku32.discordlink.core.framework.dependency.annotation.Dependency;
import com.github.riku32.discordlink.core.framework.dependency.exceptions.DependencyNotFoundException;
import com.github.riku32.discordlink.core.framework.dependency.exceptions.DependencyNotNullException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;

public class Injector {
    private final IdentityHashMap<Class<?>, Object> unnamedDependencies = new IdentityHashMap<>();
    private final HashMap<String, Object> namedDependencies = new HashMap<>();

    public void registerDependency(Class<?> type, Object value) {
        unnamedDependencies.put(type, value);
    }

    public void registerNamedDependency(String key, Object value) {
        namedDependencies.put(key, value);
    }

    /**
     * Inject dependencies into class instance
     * @param toInject object to inject dependencies into
     * @throws DependencyNotNullException throws if a field registered as a dependency was not null
     * @throws DependencyNotFoundException throws if a required dependency was not registered
     */
    public void injectDependencies(Object toInject) throws DependencyNotNullException, DependencyNotFoundException {
        for (Field field : toInject.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            Dependency dependencyAnnotation = field.getAnnotation(Dependency.class);
            if (dependencyAnnotation == null) continue;

            try {
                if (field.get(toInject) != null)
                    throw new DependencyNotNullException(field);
            } catch (IllegalAccessException ignored) {}

            // Unnamed dependency injection
            Object dependency;
            if (dependencyAnnotation.named().equals("")) {
                dependency = unnamedDependencies.get(field.getType());
                if (dependency == null)
                    throw new DependencyNotFoundException(String.format("Field %s on class %s did not have an available dependency of type %s to inject",
                            field.getName(), toInject.getClass().getName(), field.getType().getSimpleName()));
            } else {
                dependency = namedDependencies.get(dependencyAnnotation.named());
                if (dependency == null)
                    throw new DependencyNotFoundException(String.format("Field %s on class %s did not have an available dependency named %s",
                            field.getName(), toInject.getClass().getName(), dependencyAnnotation.named()));
            }

            try {
                field.set(toInject, dependency);
            } catch (IllegalAccessException ignore) {}
        }
    }
}
