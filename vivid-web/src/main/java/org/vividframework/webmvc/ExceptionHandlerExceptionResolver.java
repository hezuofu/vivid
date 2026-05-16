package org.vividframework.webmvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.BeanFactory;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.resolver.HandlerExceptionResolver;
import org.vividframework.web.model.ModelAndView;
import org.vividframework.web.view.View;
import org.vividframework.webmvc.annotation.ControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves exceptions by delegating to @ExceptionHandler methods in @ControllerAdvice beans.
 * Scans the application context for advice beans and caches their exception handler mappings.
 * @author sketch
 */
public class ExceptionHandlerExceptionResolver implements HandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerExceptionResolver.class);

    private final List<ControllerAdviceResolver> adviceResolvers = new ArrayList<>();
    private GenericApplicationContext applicationContext;

    public ExceptionHandlerExceptionResolver() {}

    public ExceptionHandlerExceptionResolver(GenericApplicationContext context) {
        this.applicationContext = context;
        initAdviceResolvers();
    }

    public void setApplicationContext(GenericApplicationContext context) {
        this.applicationContext = context;
        initAdviceResolvers();
    }

    private void initAdviceResolvers() {
        if (applicationContext == null) return;
        adviceResolvers.clear();
        try {
            String[] names = applicationContext.getBeanNamesForAnnotation(ControllerAdvice.class);
            for (String name : names) {
                try {
                    Object bean = applicationContext.getBean(name);
                    ControllerAdviceResolver resolver = new ControllerAdviceResolver(bean, applicationContext);
                    if (!resolver.getExceptionResolvers().isEmpty()) {
                        adviceResolvers.add(resolver);
                        logger.debug("Registered @ControllerAdvice: {}", bean.getClass().getName());
                    }
                } catch (Exception e) {
                    logger.debug("Failed to init ControllerAdvice bean: {}", name, e);
                }
            }
        } catch (Exception e) {
            logger.debug("No @ControllerAdvice beans found");
        }
    }

    @Override
    public ModelAndView resolveException(HttpServerRequest request, Exception exception) {
        for (ControllerAdviceResolver advice : adviceResolvers) {
            ExceptionHandlerMethodResolver handlerResolver = advice.resolveExceptionMethod(exception);
            if (handlerResolver != null) {
                try {
                    logger.debug("Resolving {} via @ExceptionHandler in {}",
                            exception.getClass().getSimpleName(),
                            advice.getBean().getClass().getSimpleName());

                    Object result = handlerResolver.resolve(request, exception);

                    if (result instanceof ModelAndView mav) {
                        return mav;
                    }
                    if (result instanceof String viewName) {
                        return new ModelAndView(viewName);
                    }
                    if (result != null) {
                        ModelAndView mav = new ModelAndView();
                        mav.addObject("error", result);
                        mav.addObject("exception", exception);
                        return mav;
                    }
                } catch (Exception e) {
                    logger.error("Error invoking @ExceptionHandler", e);
                }
            }
        }
        return null;
    }

    public List<ControllerAdviceResolver> getAdviceResolvers() {
        return adviceResolvers;
    }

    public void addAdviceResolver(ControllerAdviceResolver resolver) {
        adviceResolvers.add(resolver);
    }
}
