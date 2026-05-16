package org.vividframework.context;

import org.vividframework.beans.ListableBeanFactory;

/**
 * Application context interface
 * @author sketch
 */
public interface ApplicationContext extends ListableBeanFactory {

    /**
     * Get context ID
     */
    String getId();

    /**
     * Get display name
     */
    String getDisplayName();

    /**
     * Get parent context
     */
    ApplicationContext getParent();

    /**
     * Get environment
     */
    org.vividframework.config.Environment getEnvironment();

    /**
     * Start dynamic refresh
     */
    void start();

    /**
     * Stop dynamic refresh
     */
    void stop();

    /**
     * Check if active
     */
    boolean isActive();

    /**
     * Check if running
     */
    boolean isRunning();

    /**
     * Close context
     */
    void close();
}
