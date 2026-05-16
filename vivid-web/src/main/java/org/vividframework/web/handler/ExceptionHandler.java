package org.vividframework.web.handler;

import org.vividframework.http.HttpServerRequest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Type-safe exception handler interface.
 * Implement this and register as a bean to participate in the global exception handling chain.
 * No annotations needed — discovery is interface-based.
 *
 * <pre>
 * &#64;Component
 * public class MyValidationHandler implements ExceptionHandler&lt;ValidationException&gt; {
 *     public Object handle(ValidationException ex, HttpServerRequest request) {
 *         return ProblemDetail.badRequest(ex.getMessage())
 *                 .detail(ex.getFieldErrors());
 *     }
 * }
 * </pre>
 *
 * @param <T> the exception type this handler handles
 * @author sketch
 */
public interface ExceptionHandler<T extends Exception> {

    /**
     * Handle the exception and return a result.
     * @return ProblemDetail, ModelAndView, HttpServletResponse, or any object (treated as @ResponseBody)
     */
    Object handle(T exception, HttpServerRequest request);

    /**
     * Whether this handler supports the given exception.
     * Default checks if the exception is assignable to the generic type parameter.
     */
    @SuppressWarnings("unchecked")
    default boolean supports(Exception exception) {
        return getExceptionType().isInstance(exception);
    }

    /**
     * Priority order. Lower values = higher priority. Default 0.
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Extract the exception type from the generic parameter.
     */
    @SuppressWarnings("unchecked")
    default Class<T> getExceptionType() {
        Type[] interfaces = getClass().getGenericInterfaces();
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType pt
                    && pt.getRawType() == ExceptionHandler.class) {
                Type[] args = pt.getActualTypeArguments();
                if (args.length > 0 && args[0] instanceof Class) {
                    return (Class<T>) args[0];
                }
            }
        }
        return (Class<T>) Exception.class;
    }
}
