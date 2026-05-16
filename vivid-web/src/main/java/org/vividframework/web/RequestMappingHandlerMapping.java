package org.vividframework.web;

import org.vividframework.web.handler.HandlerExecutionChain;
import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.web.mapping.HandlerMapping.AbstractHandlerMapping;
import org.vividframework.web.mapping.RequestMappingInfo;
import org.vividframework.http.HttpMethod;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.beans.annotation.Controller;
import org.vividframework.web.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Request mapping handler mapping with URI template and meta-annotation support.
 * @author Jon Fisher
 */
public class RequestMappingHandlerMapping extends AbstractHandlerMapping {

    private final GenericApplicationContext applicationContext;
    private final Map<RequestMappingInfo, HandlerMethod> handlerMethods = new ConcurrentHashMap<>();
    private final List<MappingEntry> mappings = new ArrayList<>();

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{([^/]+)\\}");

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

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            for (String pattern : entry.getKey().getPatterns()) {
                mappings.add(new MappingEntry(pattern, entry.getKey(), entry.getValue()));
            }
        }
        // Sort: longer patterns first, exact matches before templates
        mappings.sort((a, b) -> {
            int cmp = Integer.compare(b.pattern.length(), a.pattern.length());
            if (cmp != 0) return cmp;
            return Boolean.compare(a.hasTemplate, b.hasTemplate);
        });
    }

    protected void detectHandlerMethods(Object handler) {
        Class<?> handlerType = handler.getClass();
        Method[] methods = handlerType.getDeclaredMethods();
        for (Method method : methods) {
            RequestMapping annotation = findRequestMappingAnnotation(method);
            if (annotation != null) {
                RequestMappingInfo info = createRequestMappingInfo(annotation);
                HandlerMethod handlerMethod = new HandlerMethod(handler, method);
                handlerMethods.put(info, handlerMethod);
            }
        }
    }

    /**
     * Find @RequestMapping annotation, traversing meta-annotations.
     * Supports @GetMapping, @PostMapping, etc. annotated with @RequestMapping.
     */
    protected RequestMapping findRequestMappingAnnotation(Method method) {
        // Direct annotation
        RequestMapping direct = method.getAnnotation(RequestMapping.class);
        if (direct != null) {
            return direct;
        }
        // Check meta-annotations on other annotations present on the method
        for (Annotation ann : method.getAnnotations()) {
            Class<? extends Annotation> annType = ann.annotationType();
            // Skip java.lang annotations
            if (annType.getName().startsWith("java.lang")) {
                continue;
            }
            RequestMapping meta = annType.getAnnotation(RequestMapping.class);
            if (meta != null) {
                // Merge: use the meta @RequestMapping as base, override with composed annotation values
                return createMergedAnnotation(meta, ann);
            }
        }
        return null;
    }

    /**
     * Create a virtual @RequestMapping by merging meta-annotation defaults
     * with values from the composed annotation.
     */
    private RequestMapping createMergedAnnotation(RequestMapping base, Annotation composed) {
        String composedValue = getAnnotationString(composed, "value");
        String[] composedPath = getAnnotationStringArray(composed, "path");

        return new RequestMapping() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return RequestMapping.class;
            }

            @Override
            public String name() {
                return base.name();
            }

            @Override
            public String value() {
                if (composedValue != null && !composedValue.isEmpty()) {
                    return composedValue;
                }
                if (composedPath != null && composedPath.length > 0) {
                    return composedPath[0];
                }
                return base.value();
            }

            @Override
            public String[] path() {
                if (composedPath != null && composedPath.length > 0) {
                    return composedPath;
                }
                String v = value();
                return v.isEmpty() ? new String[0] : new String[]{v};
            }

            @Override
            public HttpMethod[] method() {
                return base.method();
            }

            @Override
            public String[] produces() {
                return base.produces();
            }

            @Override
            public String[] consumes() {
                return base.consumes();
            }

            @Override
            public String[] headers() {
                return base.headers();
            }

            @Override
            public String[] params() {
                return base.params();
            }
        };
    }

    private static String getAnnotationString(Annotation ann, String methodName) {
        try {
            Method m = ann.annotationType().getMethod(methodName);
            Object result = m.invoke(ann);
            if (result instanceof String) return (String) result;
            if (result instanceof String[] arr && arr.length > 0) return arr[0];
        } catch (Exception ignored) {}
        return null;
    }

    private static String[] getAnnotationStringArray(Annotation ann, String methodName) {
        try {
            Method m = ann.annotationType().getMethod(methodName);
            Object result = m.invoke(ann);
            if (result instanceof String[] arr) return arr;
            if (result instanceof String s) return new String[]{s};
        } catch (Exception ignored) {}
        return new String[0];
    }

    protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation) {
        RequestMappingInfo.Builder builder = RequestMappingInfo.builder()
                .name(annotation.name());

        // value() returns String, path() returns String[]
        String[] patterns = annotation.path();
        if (patterns.length == 0) {
            String v = annotation.value();
            patterns = v.isEmpty() ? new String[0] : new String[]{v};
        }
        builder.patterns(patterns);

        if (annotation.method().length > 0) {
            builder.methods(annotation.method());
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

        for (MappingEntry entry : mappings) {
            Map<String, String> vars = matchWithVariables(entry.pattern, path);
            if (vars != null) {
                RequestMappingInfo info = entry.info;
                if (info.getMethods().isEmpty() || info.getMethods().contains(method)) {
                    // Set path variables on the request
                    if (!vars.isEmpty() && request instanceof org.vividframework.http.HttpRequest) {
                        ((org.vividframework.http.HttpRequest) request).setPathVariables(vars);
                    }
                    return new HandlerExecutionChain(entry.handlerMethod);
                }
            }
        }
        return null;
    }

    /**
     * Match a pattern with {variables} against a path.
     * Returns a map of variable names to values, or null if no match.
     */
    private Map<String, String> matchWithVariables(String pattern, String path) {
        if (!pattern.contains("{")) {
            // Simple match: exact, wildcard, or prefix
            if (matchSimple(pattern, path)) {
                return Collections.emptyMap();
            }
            return null;
        }

        // Convert template pattern to regex and extract variable names
        List<String> varNames = new ArrayList<>();
        Matcher m = TEMPLATE_PATTERN.matcher(pattern);
        StringBuffer regex = new StringBuffer();
        while (m.find()) {
            varNames.add(m.group(1));
            m.appendReplacement(regex, "([^/]+)");
        }
        m.appendTail(regex);

        // Also support ** wildcard in template patterns
        String regexStr = regex.toString().replace("**", ".*").replace("*", "[^/]*");

        Pattern compiled = Pattern.compile(regexStr);
        Matcher pathMatcher = compiled.matcher(path);
        if (pathMatcher.matches()) {
            Map<String, String> vars = new LinkedHashMap<>();
            for (int i = 0; i < varNames.size() && i + 1 <= pathMatcher.groupCount(); i++) {
                vars.put(varNames.get(i), pathMatcher.group(i + 1));
            }
            return vars;
        }
        return null;
    }

    private boolean matchSimple(String pattern, String path) {
        if (pattern.equals(path)) {
            return true;
        }
        if (pattern.contains("*")) {
            String regex = pattern.replace("**", ".*").replace("*", "[^/]*");
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

    private static class MappingEntry {
        final String pattern;
        final RequestMappingInfo info;
        final HandlerMethod handlerMethod;
        final boolean hasTemplate;

        MappingEntry(String pattern, RequestMappingInfo info, HandlerMethod handlerMethod) {
            this.pattern = pattern;
            this.info = info;
            this.handlerMethod = handlerMethod;
            this.hasTemplate = pattern.contains("{");
        }
    }
}
