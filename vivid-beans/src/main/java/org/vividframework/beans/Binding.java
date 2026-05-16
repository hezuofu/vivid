package org.vividframework.beans;

import java.lang.annotation.Annotation;

/**
 * A binding from a Key to a target (implementation, instance, or provider).
 * @author sketch
 */
public class Binding<T> {

    private final Key<T> key;
    private final Class<? extends T> implementation;
    private final T instance;
    private final Provider<? extends T> provider;
    private final Class<? extends Provider<? extends T>> providerClass;
    private final Class<? extends Annotation> scope;

    public Binding(Key<T> key, Class<? extends T> implementation, T instance,
                   Provider<? extends T> provider,
                   Class<? extends Provider<? extends T>> providerClass,
                   Class<? extends Annotation> scope) {
        this.key = key;
        this.implementation = implementation;
        this.instance = instance;
        this.provider = provider;
        this.providerClass = providerClass;
        this.scope = scope;
    }

    public Key<T> getKey() { return key; }
    public Class<? extends T> getImplementation() { return implementation; }
    public T getInstance() { return instance; }
    public Provider<? extends T> getProvider() { return provider; }
    public Class<? extends Provider<? extends T>> getProviderClass() { return providerClass; }
    public Class<? extends Annotation> getScope() { return scope; }
    public boolean isSingleton() { return scope != null; }

    @Override
    public String toString() {
        return "Bind[" + key + " → "
                + (implementation != null ? implementation.getSimpleName()
                    : instance != null ? "instance"
                    : provider != null ? provider.getClass().getSimpleName()
                    : providerClass != null ? providerClass.getSimpleName()
                    : "?")
                + (scope != null ? " @" + scope.getSimpleName() : "")
                + "]";
    }
}
