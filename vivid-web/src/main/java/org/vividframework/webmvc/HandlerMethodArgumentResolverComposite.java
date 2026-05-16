package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;

import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Composite implementation of HandlerMethodArgumentResolver
 * @author sketch
 */
public class HandlerMethodArgumentResolverComposite implements HandlerMethodArgumentResolver {

    private final List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

    public HandlerMethodArgumentResolverComposite addResolver(HandlerMethodArgumentResolver resolver) {
        this.resolvers.add(resolver);
        return this;
    }

    public HandlerMethodArgumentResolverComposite addResolvers(List<? extends HandlerMethodArgumentResolver> resolvers) {
        this.resolvers.addAll(resolvers);
        return this;
    }

    public HandlerMethodArgumentResolverComposite addResolvers(HandlerMethodArgumentResolver... resolvers) {
        this.resolvers.addAll(Arrays.asList(resolvers));
        return this;
    }

    public void clear() {
        this.resolvers.clear();
    }

    public int getResolverCount() {
        return this.resolvers.size();
    }

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        return getResolver(parameter) != null;
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   Parameter parameter) throws Exception {
        HandlerMethodArgumentResolver resolver = getResolver(parameter);
        if (resolver == null) {
            throw new IllegalStateException(
                "No suitable resolver found for parameter: " + parameter.getName());
        }
        return resolver.resolveArgument(handlerMethod, request, parameter);
    }

    public Object resolveArgument(Parameter parameter, HttpServerRequest request) throws Exception {
        HandlerMethodArgumentResolver resolver = getResolver(parameter);
        if (resolver != null) {
            return resolver.resolveArgument(null, request, parameter);
        }
        return null;
    }

    private HandlerMethodArgumentResolver getResolver(Parameter parameter) {
        for (HandlerMethodArgumentResolver resolver : this.resolvers) {
            if (resolver.supports(null, parameter)) {
                return resolver;
            }
        }
        return null;
    }

    /**
     * Get all registered resolvers
     */
    public List<HandlerMethodArgumentResolver> getResolvers() {
        return Collections.unmodifiableList(this.resolvers);
    }
}
