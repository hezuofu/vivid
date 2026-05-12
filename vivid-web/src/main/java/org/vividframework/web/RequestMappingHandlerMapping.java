package org.vividframework.web;

import org.vividframework.handler.HandlerExecutionChain;
import org.vividframework.handler.HandlerMethod;
import org.vividframework.mapping.HandlerMapping.AbstractHandlerMapping;
import org.vividframework.mapping.RequestMappingInfo;
import org.vividframework.http.HttpMethod;
import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.beans.annotation.Controller;
import org.vividframework.web.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Request mapping handler mapping
 * @author Jon Fisher
 */
public class RequestMappingHandlerMapping extends AbstractHandlerMapping {

    private final GenericApplicationContext applicationContext;
    private final Map<RequestMappingInfo, HandlerMethod> handlerMethods = new ConcurrentHashMap<>();
    private final Map<String, RequestMappingInfo> urlMap = new TreeMap<>(Comparator.comparing(String::length).reversed());

    public RequestMappingHandlerMapping(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        init();
    }

    private void init() {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(Controller.class);
        for (String beanName : beanNames) {
            try {
                Object controller = applicationContext.getBean(beanName);
                detectHandlerMethods(controller);
            } catch (Exception e) {
                // Ignore
            }
        }

        // Build URL map
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            for (String pattern : entry.getKey().getPatterns()) {
                urlMap.put(pattern, entry.getKey());
            }
        }
    }

    protected void detectHandlerMethods(Object handler) {
        Class<?> handlerType = handler.getClass();
        Method[] methods = handlerType.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                RequestMappingInfo info = createRequestMappingInfo(annotation);
                HandlerMethod handlerMethod = new HandlerMethod(handler, method);
                handlerMethods.put(info, handlerMethod);
            }
        }
    }

    protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation) {
        RequestMappingInfo.Builder builder = RequestMappingInfo.builder()
                .name(annotation.name())
                .patterns(annotation.value());

        if (annotation.method().length > 0) {
            builder.methods(annotation.method());
        }

        if (annotation.path().length > 0) {
            builder.patterns(annotation.path());
        }

        if (annotation.produces().length > 0) {
            builder.produces(annotation.produces());
        }

        if (annotation.consumes().length > 0) {
            builder.consumes(annotation.consumes());
        }

        if (annotation.headers().length > 0) {
            builder.headers(annotation.headers());
        }

        return builder.build();
    }

    @Override
    protected Object getHandlerInternal(HttpServerRequest request) throws Exception {
        String path = request.getPath();
        HttpMethod method = request.getMethod();

        RequestMappingInfo match = null;
        for (Map.Entry<String, RequestMappingInfo> entry : urlMap.entrySet()) {
            if (matchPattern(entry.getKey(), path)) {
                RequestMappingInfo info = entry.getValue();
                if (info.getMethods().isEmpty() || info.getMethods().contains(method)) {
                    match = info;
                    break;
                }
            }
        }

        if (match != null) {
            HandlerMethod handlerMethod = handlerMethods.get(match);
            if (handlerMethod != null) {
                return new HandlerExecutionChain(handlerMethod);
            }
        }

        return null;
    }

    private boolean matchPattern(String pattern, String path) {
        if (pattern.equals(path)) {
            return true;
        }
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return path.matches(regex);
        }
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.startsWith(prefix);
        }
        return false;
    }

    public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap(handlerMethods);
    }
}
