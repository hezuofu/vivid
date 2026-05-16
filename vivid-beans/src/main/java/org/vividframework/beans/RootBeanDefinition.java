package org.vividframework.beans;

import org.vividframework.binding.MutablePropertyValues;
import org.vividframework.binding.PropertyValues;

import java.util.*;

/**
 * Root bean definition
 * @author sketch
 */
public class RootBeanDefinition implements BeanDefinition {

    private String beanClassName;
    private Class<?> beanClass;
    private String factoryMethodName;
    private String factoryBeanName;
    private String scope = SCOPE_SINGLETON;
    private boolean abstractFlag = false;
    private int role = ROLE_APPLICATION;
    private int order = Ordered.LOWEST_PRECEDENCE;
    private Object primary;
    private boolean lazyInit = false;
    private String[] dependsOn = new String[0];
    private String description;
    private ConstructorArgumentValues constructorArgumentValues;
    private PropertyValues propertyValues;
    private MethodOverrides methodOverrides = new MethodOverrides();
    private volatile Object instance;

    public static final String SCOPE_SINGLETON = "singleton";
    public static final String SCOPE_PROTOTYPE = "prototype";

    public RootBeanDefinition() {
        this.constructorArgumentValues = new ConstructorArgumentValues();
        this.propertyValues = new MutablePropertyValues();
    }

    public RootBeanDefinition(Class<?> beanClass) {
        this();
        this.beanClass = beanClass;
        this.beanClassName = beanClass.getName();
    }

    public RootBeanDefinition(String beanClassName) {
        this();
        this.beanClassName = beanClassName;
    }

    public RootBeanDefinition(String beanClassName, ConstructorArgumentValues constructorArgumentValues,
                               PropertyValues propertyValues) {
        this.beanClassName = beanClassName;
        this.constructorArgumentValues = constructorArgumentValues;
        this.propertyValues = propertyValues;
    }

    @Override
    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        this.beanClassName = beanClass.getName();
    }

    @Override
    public String getFactoryMethodName() {
        return factoryMethodName;
    }

    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    @Override
    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    @Override
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(scope);
    }

    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(scope);
    }

    @Override
    public boolean isAbstract() {
        return abstractFlag;
    }

    public void setAbstract(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    @Override
    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public Object getPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary ? Boolean.TRUE : null;
    }

    @Override
    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    @Override
    public String[] getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String[] dependsOn) {
        this.dependsOn = dependsOn;
    }

    @Override
    public boolean hasDependsOn() {
        return dependsOn != null && dependsOn.length > 0;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ConstructorArgumentValues getConstructorArgumentValues() {
        return constructorArgumentValues;
    }

    public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
        this.constructorArgumentValues = constructorArgumentValues;
    }

    public PropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }

    public MethodOverrides getMethodOverrides() {
        return methodOverrides;
    }

    public void setMethodOverrides(MethodOverrides methodOverrides) {
        this.methodOverrides = methodOverrides;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public boolean hasConstructorArgumentValues() {
        return constructorArgumentValues != null && !constructorArgumentValues.isEmpty();
    }

    public boolean hasPropertyValues() {
        return propertyValues != null && !propertyValues.isEmpty();
    }

    public interface Ordered {
        int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
        int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    }

    /**
     * Constructor argument values
     */
    public static class ConstructorArgumentValues {
        private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<>();
        private final List<ValueHolder> genericArgumentValues = new ArrayList<>();

        public void addArgumentValue(Object value) {
            genericArgumentValues.add(new ValueHolder(value));
        }

        public void addArgumentValue(Integer index, Object value) {
            indexedArgumentValues.put(index, new ValueHolder(value));
        }

        public void addArgumentValue(Integer index, Object value, String type) {
            indexedArgumentValues.put(index, new ValueHolder(value, type));
        }

        public Map<Integer, ValueHolder> getIndexedArgumentValues() {
            return indexedArgumentValues;
        }

        public List<ValueHolder> getGenericArgumentValues() {
            return genericArgumentValues;
        }

        public boolean isEmpty() {
            return indexedArgumentValues.isEmpty() && genericArgumentValues.isEmpty();
        }

        public int getArgumentCount() {
            return indexedArgumentValues.size() + genericArgumentValues.size();
        }

        public static class ValueHolder {
            private Object value;
            private String type;
            private String name;

            public ValueHolder(Object value) {
                this.value = value;
            }

            public ValueHolder(Object value, String type) {
                this.value = value;
                this.type = type;
            }

            public Object getValue() {
                return value;
            }

            public void setValue(Object value) {
                this.value = value;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }

    /**
     * Method overrides for lookup methods
     */
    public static class MethodOverrides {
        private final Map<String, Override> overrides = new LinkedHashMap<>();

        public void addOverride(Override override) {
            overrides.put(override.getMethodName(), override);
        }

        public Override getOverride(String methodName) {
            return overrides.get(methodName);
        }

        public Collection<Override> getOverrides() {
            return overrides.values();
        }

        public boolean isEmpty() {
            return overrides.isEmpty();
        }

        public abstract static class Override {
            private String methodName;

            public Override(String methodName) {
                this.methodName = methodName;
            }

            public String getMethodName() {
                return methodName;
            }
        }

        public static class LookupOverride extends Override {
            private String beanName;

            public LookupOverride(String methodName, String beanName) {
                super(methodName);
                this.beanName = beanName;
            }

            public String getBeanName() {
                return beanName;
            }
        }

        public static class ReplaceOverride extends Override {
            private String beanClass;

            public ReplaceOverride(String methodName, String beanClass) {
                super(methodName);
                this.beanClass = beanClass;
            }

            public String getBeanClass() {
                return beanClass;
            }
        }
    }
}
