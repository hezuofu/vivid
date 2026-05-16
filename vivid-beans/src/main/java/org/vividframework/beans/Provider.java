package org.vividframework.beans;

/**
 * Provider of instances of type T.
 * Similar to Guice's Provider and javax.inject.Provider.
 *
 * @author sketch
 */
@FunctionalInterface
public interface Provider<T> {

    /**
     * Provide an instance of T.
     */
    T get();
}
