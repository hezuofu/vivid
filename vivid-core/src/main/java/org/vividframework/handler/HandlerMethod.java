package org.vividframework.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler method representation (similar to Spring's HandlerMethod)
 * @author Jon Fisher
 */
public class HandlerMethod {

    private final Object bean;
    private final Method method;
    private final Class<?> beanType;
    private final String methodName;
    private final MethodParameter[] parameters;
    private final Map<String, Object> attributes = new HashMap<>();

    public HandlerMethod(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
        this.beanType = bean.getClass();
        this.methodName = method.getName();
        this.parameters = resolveParameters(method);
    }

    public HandlerMethod(HandlerMethod other, Object handler) {
        this.bean = handler;
        this.method = other.method;
        this.beanType = handler.getClass();
        this.methodName = other.methodName;
        this.parameters = other.parameters;
    }

    private static MethodParameter[] resolveParameters(Method method) {
        Parameter[] params = method.getParameters();
        MethodParameter[] parameters = new MethodParameter[params.length];
        for (int i = 0; i < params.length; i++) {
            parameters[i] = new MethodParameter(params[i], i);
        }
        return parameters;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodParameter[] getParameters() {
        return parameters;
    }

    public int getParameterCount() {
        return parameters.length;
    }

    public MethodParameter getParameter(int index) {
        return parameters[index];
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    public String getDescription() {
        return beanType.getName() + "." + methodName;
    }

    public String getShortDescription() {
        return beanType.getSimpleName() + "." + methodName;
    }

    /**
     * Check if the handler method has the specified annotation
     * @param annotationType the annotation type to check
     * @return true if the method has the annotation
     */
    public <T extends java.lang.annotation.Annotation> boolean hasMethodAnnotation(Class<T> annotationType) {
        return method.isAnnotationPresent(annotationType);
    }

    /**
     * Get method annotation if present
     * @param annotationType the annotation type to retrieve
     * @return the annotation or null if not present
     */
    public <T extends java.lang.annotation.Annotation> T getMethodAnnotation(Class<T> annotationType) {
        return method.getAnnotation(annotationType);
    }

    @Override
    public String toString() {
        return getDescription() + "(" + Arrays.toString(parameters) + ")";
    }

    /**
     * Method parameter information
     */
    public static class MethodParameter {
        private final Parameter parameter;
        private final int parameterIndex;
        private final Class<?> parameterType;
        private final String parameterName;
        private final Object[] annotations;

        public MethodParameter(Parameter parameter, int parameterIndex) {
            this.parameter = parameter;
            this.parameterIndex = parameterIndex;
            this.parameterType = parameter.getType();
            this.parameterName = parameter.getName();
            this.annotations = parameter.getAnnotations();
        }

        public Parameter getParameter() {
            return parameter;
        }

        public int getParameterIndex() {
            return parameterIndex;
        }

        public Class<?> getParameterType() {
            return parameterType;
        }

        public String getParameterName() {
            return parameterName;
        }

        @SuppressWarnings("unchecked")
        public <T extends java.lang.annotation.Annotation> T[] getAnnotations(Class<T> annotationType) {
            return (T[]) parameter.getAnnotationsByType(annotationType);
        }

        public boolean hasAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) {
            return parameter.isAnnotationPresent(annotationType);
        }

        public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationType) {
            return parameter.getAnnotation(annotationType);
        }

        public boolean isType(Class<?> type) {
            return type.isAssignableFrom(parameterType);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Object ann : annotations) {
                sb.append(ann).append(" ");
            }
            sb.append(parameterType.getSimpleName()).append(" ").append(parameterName);
            return sb.toString();
        }
    }
}
