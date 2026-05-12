package org.vividframework.webmvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.handler.HandlerAdapter;
import org.vividframework.handler.HandlerExecutionChain;
import org.vividframework.handler.HandlerMethod;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.http.server.HttpServerResponse;
import org.vividframework.model.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handler adapter for request mapping handler methods
 * @author Jon Fisher
 */
public class RequestMappingHandlerAdapter implements HandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RequestMappingHandlerAdapter.class);

    private HandlerMethodArgumentResolverComposite argumentResolvers;
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;
    private List<ControllerAdviceResolver> controllerAdviceResolvers;

    public RequestMappingHandlerAdapter() {
        this.argumentResolvers = createDefaultArgumentResolvers();
        this.returnValueHandlers = createDefaultReturnValueHandlers();
        this.controllerAdviceResolvers = new ArrayList<>();
    }

    private HandlerMethodArgumentResolverComposite createDefaultArgumentResolvers() {
        HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();
        resolvers.addResolvers(
            new PathVariableMethodArgumentResolver(),
            new RequestBodyMethodArgumentResolver(),
            new RequestParamMethodArgumentResolver(),
            new RequestHeaderMethodArgumentResolver()
        );
        return resolvers;
    }

    private HandlerMethodReturnValueHandlerComposite createDefaultReturnValueHandlers() {
        HandlerMethodReturnValueHandlerComposite handlers = new HandlerMethodReturnValueHandlerComposite();
        handlers.addHandlers(
            new RequestResponseBodyMethodProcessor(),
            new ViewMethodReturnValueHandler(),
            new VoidMethodReturnValueHandler()
        );
        return handlers;
    }

    @Override
    public boolean supports(Object handler) {
        return handler instanceof HandlerMethod;
    }

    @Override
    public ModelAndView handle(HttpServerRequest request, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        HandlerExecutionChain executionChain = new HandlerExecutionChain(handlerMethod);

        // Get the actual handler method (might be wrapped by controller advice)
        InvocableHandlerMethod invocable = getInvocableHandlerMethod(handlerMethod);
        invocable.setArgumentResolvers(this.argumentResolvers);

        Object returnValue = invocable.invoke(request);

        if (returnValue instanceof CompletableFuture) {
            // Handle async return values
            return handleAsyncReturnValue(handlerMethod, request, (CompletableFuture<?>) returnValue);
        }

        // Handle synchronous return values
        return handleReturnValue(handlerMethod, request, returnValue);
    }

    protected InvocableHandlerMethod getInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new InvocableHandlerMethod(handlerMethod);
    }

    protected ModelAndView handleReturnValue(HandlerMethod handlerMethod,
                                             HttpServerRequest request,
                                             Object returnValue) throws Exception {
        Class<?> returnType = handlerMethod.getMethod().getReturnType();

        if (void.class.equals(returnType) || Void.class.equals(returnType)) {
            return null;
        }

        if (returnValue instanceof ModelAndView) {
            return (ModelAndView) returnValue;
        }

        // For @ResponseBody methods, the return value is handled differently
        HandlerMethodReturnValueHandler handler = this.returnValueHandlers.getHandler(returnType);
        if (handler != null) {
            HttpServerResponse response = HttpServerResponse.from(() -> HttpServletResponse.ok());
            handler.handleReturnValue(handlerMethod, request, response, returnValue);
            return new ModelAndView();
        }

        // Default: return ModelAndView with view from return value
        ModelAndView mav = new ModelAndView();
        mav.addObject("result", returnValue);
        return mav;
    }

    protected ModelAndView handleAsyncReturnValue(HandlerMethod handlerMethod,
                                                  HttpServerRequest request,
                                                  CompletableFuture<?> future) throws Exception {
        // For async methods, return null and let the framework handle it
        // In a full implementation, this would register a callback
        return null;
    }

    public void setArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
    }

    public HandlerMethodArgumentResolverComposite getArgumentResolvers() {
        return argumentResolvers;
    }

    public void setReturnValueHandlers(HandlerMethodReturnValueHandlerComposite returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }

    public HandlerMethodReturnValueHandlerComposite getReturnValueHandlers() {
        return returnValueHandlers;
    }

    public void setControllerAdviceResolvers(List<ControllerAdviceResolver> controllerAdviceResolvers) {
        this.controllerAdviceResolvers = controllerAdviceResolvers;
    }
}
