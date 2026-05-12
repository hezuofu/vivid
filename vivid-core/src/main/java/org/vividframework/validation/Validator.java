package org.vividframework.validation;

/**
 * Validator interface
 * @author Jon Fisher
 */
public interface Validator {

    /**
     * Check if this validator supports the given class
     */
    boolean supports(Class<?> clazz);

    /**
     * Validate the target object
     */
    void validate(Object target, Errors errors);
}
