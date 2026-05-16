package org.vividframework.beans;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Type-safe binding key combining a type with an optional qualifier annotation.
 * Similar to Guice's Key.
 *
 * <pre>
 * Key&lt;Service&gt; plain = Key.of(Service.class);
 * Key&lt;Service&gt; named = Key.of(Service.class, Names.named("primary"));
 * </pre>
 *
 * @author sketch
 */
public final class Key<T> {

    private final Class<T> type;
    private final Class<? extends Annotation> qualifier;
    private final String name;

    private Key(Class<T> type, Class<? extends Annotation> qualifier, String name) {
        this.type = type;
        this.qualifier = qualifier;
        this.name = name;
    }

    public Class<T> getType() { return type; }
    public Class<? extends Annotation> getQualifier() { return qualifier; }
    public String getName() { return name; }
    public boolean hasQualifier() { return qualifier != null || name != null; }

    public static <T> Key<T> of(Class<T> type) {
        return new Key<>(type, null, null);
    }

    public static <T> Key<T> of(Class<T> type, Class<? extends Annotation> qualifier) {
        return new Key<>(type, qualifier, null);
    }

    public static <T> Key<T> of(Class<T> type, String name) {
        return new Key<>(type, null, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key<?> key)) return false;
        return type == key.type && Objects.equals(qualifier, key.qualifier)
                && Objects.equals(name, key.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, qualifier, name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Key[");
        sb.append(type.getSimpleName());
        if (qualifier != null) sb.append(" @").append(qualifier.getSimpleName());
        if (name != null) sb.append(" @Named(\"").append(name).append("\")");
        return sb.append("]").toString();
    }
}
