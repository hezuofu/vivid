package org.vividframework.boot.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects auto-configuration classes to import
 * @author Jon Fisher
 */
public class AutoConfigurationImportSelector {

    private static final Logger logger = LoggerFactory.getLogger(AutoConfigurationImportSelector.class);
    private static final String AUTOCONFIGURATION_PACKAGES = "META-INF/vivid/autoconfiguration.packages";
    private static final String AUTOCONFIGURATION_CLASSES = "META-INF/vivid/autoconfiguration";

    private ClassLoader classLoader;

    public AutoConfigurationImportSelector() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Get auto-configuration classes from META-INF files
     */
    public List<String> selectAutoConfigurations() {
        List<String> configurations = new ArrayList<>();
        
        // Load from autoconfiguration.packages
        configurations.addAll(loadPackageNames());
        
        // Load from autoconfiguration file
        configurations.addAll(loadConfigurationNames());
        
        return configurations;
    }

    /**
     * Load package names from autoconfiguration.packages file
     */
    protected List<String> loadPackageNames() {
        try {
            Enumeration<URL> resources = classLoader.getResources(AUTOCONFIGURATION_PACKAGES);
            List<String> packages = new ArrayList<>();
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            packages.add(line);
                        }
                    }
                }
            }
            
            return packages;
        } catch (Exception e) {
            logger.warn("Could not load auto-configuration packages", e);
            return Collections.emptyList();
        }
    }

    /**
     * Load configuration class names from autoconfiguration file
     */
    protected List<String> loadConfigurationNames() {
        try {
            Enumeration<URL> resources = classLoader.getResources(AUTOCONFIGURATION_CLASSES);
            List<String> configurations = new ArrayList<>();
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            configurations.add(line);
                        }
                    }
                }
            }
            
            return configurations;
        } catch (Exception e) {
            logger.warn("Could not load auto-configuration classes", e);
            return Collections.emptyList();
        }
    }

    /**
     * Filter configurations based on conditions
     */
    public List<String> getCandidates() {
        List<String> candidates = selectAutoConfigurations();
        
        // Remove duplicates while preserving order
        return new ArrayList<>(new LinkedHashSet<>(candidates));
    }

    /**
     * Exclude specific configurations
     */
    public List<String> excludeConfigurations(List<String> excludes) {
        List<String> candidates = getCandidates();
        candidates.removeAll(excludes);
        return candidates;
    }
}
