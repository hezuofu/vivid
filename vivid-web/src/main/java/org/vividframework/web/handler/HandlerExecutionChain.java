package org.vividframework.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.interceptor.HandlerInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handler execution chain containing handler and interceptors
 * @author Jon Fisher
 */
public class HandlerExecutionChain {

    private static final Logger logger = LoggerFactory.getLogger(HandlerExecutionChain.class);

    private final Object handler;
    private final List<HandlerInterceptor> interceptorList = new ArrayList<>();
    private int interceptorIndex = -1;

    public HandlerExecutionChain(Object handler) {
        this(handler, (HandlerInterceptor[]) null);
    }

    public HandlerExecutionChain(Object handler, HandlerInterceptor... interceptors) {
        this.handler = handler;
        if (interceptors != null) {
            this.interceptorList.addAll(Arrays.asList(interceptors));
        }
    }

    public Object getHandler() {
        return handler;
    }

    public void addInterceptor(HandlerInterceptor interceptor) {
        this.interceptorList.add(interceptor);
    }

    public void addInterceptor(int order, HandlerInterceptor interceptor) {
        this.interceptorList.add(order, interceptor);
    }

    public int getInterceptorCount() {
        return interceptorList.size();
    }

    /**
     * Apply pre-handle interceptors.
     */
    public boolean applyPreHandle(HttpServerRequest request) throws Exception {
        return applyPreHandle(request, null);
    }

    /**
     * Apply pre-handle interceptors with response.
     */
    boolean applyPreHandle(HttpServerRequest request, HttpServletResponse response) throws Exception {
        for (int i = 0; i < interceptorList.size(); i++) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            if (!interceptor.preHandle(request, response, handler)) {
                triggerAfterCompletion(request, response, null);
                return false;
            }
            interceptorIndex = i;
        }
        return true;
    }

    /**
     * Apply post-handle interceptors.
     */
    public void applyPostHandle(HttpServerRequest request, Object result) throws Exception {
        applyPostHandle(request, null, result);
    }

    /**
     * Apply post-handle interceptors with response.
     */
    void applyPostHandle(HttpServerRequest request, HttpServletResponse response, Object modelAndView) throws Exception {
        for (int i = interceptorList.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            interceptor.postHandle(request, response, handler, modelAndView);
        }
    }

    /**
     * Trigger after completion callbacks.
     */
    public void triggerAfterCompletion(HttpServerRequest request, Exception ex) {
        triggerAfterCompletion(request, null, ex);
    }

    /**
     * Trigger after completion callbacks with response.
     */
    void triggerAfterCompletion(HttpServerRequest request, HttpServletResponse response, Exception ex) {
        for (int i = Math.min(interceptorIndex, interceptorList.size() - 1); i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            try {
                interceptor.afterCompletion(request, response, handler, ex);
            } catch (Throwable e) {
                logger.error("HandlerInterceptor.afterCompletion threw exception", e);
            }
        }
    }

    /**
     * Apply async after-complete callbacks
     */
    void applyAfterConcurrentHandlingStarted(HttpServerRequest request, HttpServletResponse response) {
        if (interceptorList.isEmpty()) {
            return;
        }
        for (int i = interceptorList.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            try {
                interceptor.afterConcurrentHandlingStarted(request, response, handler);
            } catch (Throwable e) {
                logger.error("HandlerInterceptor.afterConcurrentHandlingStarted threw exception", e);
            }
        }
    }
}
