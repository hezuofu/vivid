package org.vividframework.beans.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.BeanFactory;
import org.vividframework.beans.BeanPostProcessor;
import org.vividframework.beans.DefaultListableBeanFactory;
import org.vividframework.beans.annotation.Autowired;
import org.vividframework.beans.annotation.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autowired annotation bean post processor.
 * Handles @Autowired injection for fields, methods, and constructors.
 * @author Jon Fisher
 */
public class AutowiredAnnotationBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AutowiredAnnotationBeanPostProcessor.class);

    private final Map<Class<?>, AnnotationMetadata> annotationMetadataCache = new ConcurrentHashMap<>();
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        Class<?> clazz = bean.getClass();
        AnnotationMetadata metadata = getAnnotationMetadata(clazz);
        
        // Inject constructor
        injectConstructor(bean, clazz, metadata);
        
        // Inject fields
        injectFields(bean, clazz, metadata);
        
        // Inject methods
        injectMethods(bean, clazz, metadata);
        
        return bean;
    }

    private AnnotationMetadata getAnnotationMetadata(Class<?> clazz) {
        return annotationMetadataCache.computeIfAbsent(clazz, this::buildAnnotationMetadata);
    }

    private AnnotationMetadata buildAnnotationMetadata(Class<?> clazz) {
        AnnotationMetadata metadata = new AnnotationMetadata();
        
        // Find autowired constructor
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> autowiredConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                autowiredConstructor = constructor;
                break;
            }
        }
        
        // Fallback to default constructor or single constructor
        if (autowiredConstructor == null) {
            if (constructors.length == 1) {
                autowiredConstructor = constructors[0];
            } else {
                // Find constructor with @Autowired on parameters
                for (Constructor<?> constructor : constructors) {
                    if (constructor.getParameterCount() > 0) {
                        autowiredConstructor = constructor;
                        break;
                    }
                }
            }
        }
        metadata.constructor = autowiredConstructor;
        
        // Find autowired fields
        List<Field> autowiredFields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    autowiredFields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        metadata.fields = autowiredFields.toArray(new Field[0]);
        
        // Find autowired methods (excluding constructors)
        List<Method> autowiredMethods = new ArrayList<>();
        current = clazz;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.isConstructor() && isAutowiredMethod(method)) {
                    autowiredMethods.add(method);
                }
            }
            current = current.getSuperclass();
        }
        metadata.methods = autowiredMethods.toArray(new Method[0]);
        
        return metadata;
    }

    private boolean isAutowiredMethod(Method method) {
        if (method.isAnnotationPresent(Autowired.class)) {
            return true;
        }
        // Check if any parameter has @Autowired
        for (java.lang.reflect.Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(Autowired.class)) {
                return true;
            }
        }
        return false;
    }

    private void injectConstructor(Object bean, Class<?> clazz, AnnotationMetadata metadata) throws Exception {
        Constructor<?> constructor = metadata.constructor;
        if (constructor == null) {
            return;
        }
        
        Autowired autowired = constructor.getAnnotation(Autowired.class);
        boolean required = autowired == null || autowired.required();
        
        // Check if already instantiated with this constructor
        if (bean.getClass().getDeclaredConstructors()[0] != constructor && 
            !Modifier.isPublic(constructor.getModifiers())) {
            constructor.setAccessible(true);
        }
        
        try {
            constructor.setAccessible(true);
            Object[] args = resolveConstructorArguments(constructor, required);
            
            // If constructor args resolved and bean needs reinjection, recreate
            if (args != null && args.length > 0) {
                boolean needsRecreate = false;
                for (Object arg : args) {
                    if (arg != null) {
                        needsRecreate = true;
                        break;
                    }
                }
                if (needsRecreate && constructor.getParameterCount() == args.length) {
                    Object newBean = constructor.newInstance(args);
                    // Copy field values from old bean to new bean
                    copyFieldValues(bean, newBean, clazz);
                    bean = newBean;
                }
            }
        } catch (Exception e) {
            if (required) {
                throw e;
            }
            logger.debug("Optional autowired constructor failed for {}", clazz.getName());
        }
    }

    private void copyFieldValues(Object source, Object target, Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(source);
                        field.set(target, value);
                    } catch (Exception ignored) {
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    private Object[] resolveConstructorArguments(Constructor<?> constructor, boolean required) throws Exception {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Autowired autowired = getAutowiredAnnotation(paramAnnotations[i]);
            boolean paramRequired = autowired == null || autowired.required();
            args[i] = resolveDependency(paramTypes[i], null, paramRequired);
        }
        
        return args;
    }

    private void injectFields(Object bean, Class<?> clazz, AnnotationMetadata metadata) throws Exception {
        for (Field field : metadata.fields) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            boolean required = autowired == null || autowired.required();
            
            field.setAccessible(true);
            Object value = resolveDependency(field.getType(), field.getName(), required);
            field.set(bean, value);
        }
    }

    private void injectMethods(Object bean, Class<?> clazz, AnnotationMetadata metadata) throws Exception {
        for (Method method : metadata.methods) {
            Autowired autowired = method.getAnnotation(Autowired.class);
            boolean required = autowired == null || autowired.required();
            
            Class<?>[] paramTypes = method.getParameterTypes();
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            Object[] args = new Object[paramTypes.length];
            
            for (int i = 0; i < paramTypes.length; i++) {
                Autowired paramAutowired = getAutowiredAnnotation(paramAnnotations[i]);
                boolean paramRequired = paramAutowired == null || paramAutowired.required();
                args[i] = resolveDependency(paramTypes[i], null, paramRequired);
            }
            
            method.setAccessible(true);
            method.invoke(bean, args);
        }
    }

    private Autowired getAutowiredAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Autowired) {
                return (Autowired) annotation;
            }
        }
        return null;
    }

    private Object resolveDependency(Class<?> type, String name, boolean required) throws Exception {
        if (beanFactory == null) {
            throw new IllegalStateException("BeanFactory not set for AutowiredAnnotationBeanPostProcessor");
        }
        
        // Handle array type
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            return resolveArrayDependency(componentType, required);
        }
        
        // Handle Collection type
        if (Collection.class.isAssignableFrom(type)) {
            return null; // Collection resolution handled separately
        }
        
        // Handle Map type
        if (Map.class.isAssignableFrom(type)) {
            return null; // Map resolution handled separately
        }
        
        // Try by type
        try {
            return beanFactory.getBean(type);
        } catch (Exception e) {
            // Try by name
            if (name != null) {
                try {
                    return beanFactory.getBean(name);
                } catch (Exception ignored) {
                }
            }
            if (required) {
                throw new IllegalStateException(
                    "No qualifying bean of type '" + type.getName() + "' found for dependency" +
                    (name != null ? ": bean name '" + name + "'" : ""));
            }
        }
        return null;
    }

    private Object resolveArrayDependency(Class<?> componentType, boolean required) throws Exception {
        String[] beanNames = beanFactory.getBeanNamesForType(componentType);
        if (beanNames.length == 0) {
            if (required) {
                throw new IllegalStateException("No qualifying bean of type '" + componentType.getName() + "' found");
            }
            return null;
        }
        Object array = Array.newInstance(componentType, beanNames.length);
        for (int i = 0; i < beanNames.length; i++) {
            Array.set(array, i, beanFactory.getBean(beanNames[i]));
        }
        return array;
    }

    private static class AnnotationMetadata {
        Constructor<?> constructor;
        Field[] fields = new Field[0];
        Method[] methods = new Method[0];
    }
}
