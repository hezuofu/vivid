package org.vividframework.boot.properties;

import org.vividframework.boot.properties.annotation.ConfigurationProperties;

/**
 * Base class for configuration properties
 * @author Jon Fisher
 */
public abstract class VividProperties {

    /**
     * Prefix for property names
     */
    public String getPrefix() {
        return "";
    }

    /**
     * Enable this configuration
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * Merge properties from another instance
     */
    protected void merge(VividProperties other) {
        if (other == null) {
            return;
        }
    }
}
