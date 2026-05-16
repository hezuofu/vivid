package org.vividframework.beans;

import java.util.Arrays;

/**
 * Spring-style fluent bean registration API.
 *
 * <pre>
 * registry.registerBean(Service.class, ServiceImpl.class).singleton();
 * registry.registerBean(Service.class).qualifier("primary").instance(new PrimaryService());
 * registry.registerBean(Repository.class).provider(RepoProvider.class);
 * </pre>
 *
 * @author sketch
 */
public final class BeanRegistration {

    private BeanRegistration() {}

    // --- DSL interface ---

    public interface Builder<T> {
        Builder<T> scope(String scope);
        Builder<T> singleton();
        Builder<T> prototype();
        Builder<T> lazyInit(boolean lazy);
        Builder<T> dependsOn(String... beanNames);
        Builder<T> qualifier(String qualifier);
        Builder<T> implementation(Class<? extends T> type);
        Builder<T> provider(Class<? extends Provider<? extends T>> providerType);
        Builder<T> provider(Provider<? extends T> provider);
        Builder<T> instance(T instance);
        Builder<T> initMethod(String methodName);
        Builder<T> destroyMethod(String methodName);
        void register();
    }

    // --- Static entry points ---

    public static <T> Builder<T> forType(BeanDefinitionRegistry registry, Class<T> beanType) {
        return new DefaultBuilder<>(registry, beanType);
    }

    public static <T> Builder<T> forType(BeanDefinitionRegistry registry, Class<T> beanType,
                                          Class<? extends T> implementationType) {
        return new DefaultBuilder<>(registry, beanType).implementation(implementationType);
    }

    // --- Default implementation ---

    static class DefaultBuilder<T> implements Builder<T> {
        private final BeanDefinitionRegistry registry;
        private final Class<T> beanType;
        private String beanName;
        private Class<? extends T> implementationType;
        private T instance;
        private Provider<? extends T> provider;
        private String scope = RootBeanDefinition.SCOPE_SINGLETON;
        private String qualifier;
        private boolean lazyInit;
        private String[] dependsOn = new String[0];

        DefaultBuilder(BeanDefinitionRegistry registry, Class<T> beanType) {
            this.registry = registry;
            this.beanType = beanType;
            this.beanName = Character.toLowerCase(beanType.getSimpleName().charAt(0))
                    + beanType.getSimpleName().substring(1);
        }

        @Override
        public Builder<T> scope(String s) { this.scope = s; return this; }
        @Override
        public Builder<T> singleton() { this.scope = RootBeanDefinition.SCOPE_SINGLETON; return this; }
        @Override
        public Builder<T> prototype() { this.scope = RootBeanDefinition.SCOPE_PROTOTYPE; return this; }
        @Override
        public Builder<T> lazyInit(boolean lazy) { this.lazyInit = lazy; return this; }
        @Override
        public Builder<T> dependsOn(String... names) { this.dependsOn = names; return this; }
        @Override
        public Builder<T> qualifier(String q) { this.qualifier = q; return this; }
        @Override
        public Builder<T> implementation(Class<? extends T> type) { this.implementationType = type; return this; }
        @Override
        public Builder<T> provider(Class<? extends Provider<? extends T>> type) { this.provider = null; return this; }
        @Override
        public Builder<T> provider(Provider<? extends T> p) { this.provider = p; return this; }
        @Override
        public Builder<T> instance(T inst) { this.instance = inst; return this; }
        @Override
        public Builder<T> initMethod(String m) { return this; }
        @Override
        public Builder<T> destroyMethod(String m) { return this; }

        @Override
        public void register() {
            Class<?> implType = implementationType != null ? implementationType : beanType;
            RootBeanDefinition def = new RootBeanDefinition(implType);
            def.setScope(scope);
            def.setLazyInit(lazyInit);
            def.setDependsOn(dependsOn);
            if (instance != null) def.setInstance(instance);
            String name = qualifier != null ? qualifier : beanName;
            registry.registerBeanDefinition(name, def);
        }
    }
}
