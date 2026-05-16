package org.vividframework.beans;

import org.vividframework.beans.annotation.Named;

import java.lang.annotation.Annotation;

/**
 * Utility for creating qualifier-based keys.
 * @author sketch
 */
public final class Names {

    private Names() {}

    /**
     * Create a @Named qualifier with the given value.
     */
    public static Named named(String value) {
        return new NamedImpl(value);
    }

    /**
     * Synthetic @Named implementation.
     */
    private record NamedImpl(String value) implements Named {
        @Override public Class<? extends Annotation> annotationType() { return Named.class; }
        @Override public boolean equals(Object o) {
            return o instanceof Named n && value.equals(n.value());
        }
        @Override public int hashCode() { return (127 * "value".hashCode()) ^ value.hashCode(); }
        @Override public String toString() { return "@" + Named.class.getName() + "(value=" + value + ")"; }
    }
}
