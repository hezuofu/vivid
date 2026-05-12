package org.vividframework.beans.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.BeanDefinition;
import org.vividframework.beans.BeanDefinitionRegistry;
import org.vividframework.beans.RootBeanDefinition;
import org.vividframework.beans.annotation.Component;
import org.vividframework.beans.annotation.ComponentScan;
import org.vividframework.beans.annotation.Controller;
import org.vividframework.beans.annotation.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Class path bean definition scanner.
 * Scans classpath for components annotated with @Component, @Service, @Controller, etc.
 * @author Jon Fisher
 */
public class ClassPathBeanDefinitionScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClassPathBeanDefinitionScanner.class);

    private final BeanDefinitionRegistry registry;
    private ClassLoader classLoader;
    private final Set<String> scannedPackages = ConcurrentHashMap.newKeySet();

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, ClassLoader classLoader) {
        this.registry = registry;
        this.classLoader = classLoader;
    }

    /**
     * Scan packages for bean definitions
     */
    public void scan(String... basePackages) {
        for (String basePackage : basePackages) {
            if (basePackage != null && !basePackage.isEmpty()) {
                scanPackage(basePackage.trim());
            }
        }
    }

    /**
     * Scan from ComponentScan annotation
     */
    public void scanFromComponentScan(Class<?> configurationClass) {
        ComponentScan componentScan = configurationClass.getAnnotation(ComponentScan.class);
        if (componentScan == null) {
            return;
        }

        // Get base packages
        List<String> packages = new ArrayList<>();
        
        // From value
        packages.addAll(Arrays.asList(componentScan.value()));
        
        // From basePackages
        packages.addAll(Arrays.asList(componentScan.basePackages()));
        
        // From basePackageClasses
        for (Class<?> cls : componentScan.basePackageClasses()) {
            packages.add(cls.getPackage().getName());
        }

        if (packages.isEmpty()) {
            // Default to the package of the configuration class
            packages.add(configurationClass.getPackage().getName());
        }

        scan(packages.toArray(new String[0]));
    }

    protected void scanPackage(String basePackage) {
        String packagePath = basePackage.replace('.', '/');
        
        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                
                if ("jar".equals(protocol)) {
                    scanJar(resource, basePackage);
                } else if ("file".equals(protocol)) {
                    scanDirectory(resource, basePackage);
                }
            }
        } catch (IOException e) {
            logger.warn("Could not scan package: {}", basePackage, e);
        }
    }

    protected void scanDirectory(URL resource, String basePackage) throws IOException {
        String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
        File directory = new File(filePath);
        
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file.toURI().toURL(), basePackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                processClass(className);
            }
        }
    }

    protected void scanJar(URL resource, String basePackage) throws IOException {
        String jarPath = resource.getPath();
        // Extract jar file path from URL
        int exclamationIndex = jarPath.lastIndexOf("!/");
        if (exclamationIndex > 0) {
            jarPath = jarPath.substring(0, exclamationIndex);
        }
        
        // Remove "file:" prefix
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5);
        }
        
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
        
        try (JarInputStream jarStream = new JarInputStream(new java.io.FileInputStream(jarPath))) {
            JarEntry entry;
            String packagePath = basePackage.replace('.', '/') + "/";
            
            while ((entry = jarStream.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName.substring(0, entryName.length() - 6)
                            .replace('/', '.');
                    processClass(className);
                }
            }
        } catch (IOException e) {
            logger.warn("Could not scan JAR: {}", jarPath, e);
        }
    }

    protected void processClass(String className) {
        if (scannedPackages.contains(className)) {
            return;
        }
        scannedPackages.add(className);

        try {
            Class<?> clazz = classLoader.loadClass(className);
            
            // Check if already registered
            String beanName = getBeanName(clazz);
            if (registry.containsBeanDefinition(beanName)) {
                return;
            }

            // Check for component annotations
            if (clazz.isAnnotationPresent(Component.class)) {
                registerBeanDefinition(beanName, clazz);
            } else if (clazz.isAnnotationPresent(Service.class)) {
                registerBeanDefinition(beanName, clazz);
            } else if (clazz.isAnnotationPresent(Controller.class)) {
                registerBeanDefinition(beanName, clazz);
            }

        } catch (ClassNotFoundException e) {
            logger.debug("Could not load class: {}", className);
        } catch (NoClassDefFoundError e) {
            logger.debug("Could not define class: {}", className);
        }
    }

    protected void registerBeanDefinition(String beanName, Class<?> clazz) {
        RootBeanDefinition definition = new RootBeanDefinition(clazz);
        registry.registerBeanDefinition(beanName, definition);
        logger.debug("Registered bean definition: {} -> {}", beanName, clazz.getName());
    }

    /**
     * Get bean name from class
     */
    public static String getBeanName(Class<?> clazz) {
        // Check for @Component with explicit name
        Component component = clazz.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        
        Service service = clazz.getAnnotation(Service.class);
        if (service != null && !service.value().isEmpty()) {
            return service.value();
        }
        
        Controller controller = clazz.getAnnotation(Controller.class);
        if (controller != null && !controller.value().isEmpty()) {
            return controller.value();
        }

        // Default: lowercase first letter
        String className = clazz.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    public Set<String> getScannedPackages() {
        return Collections.unmodifiableSet(scannedPackages);
    }
}
