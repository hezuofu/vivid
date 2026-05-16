package org.vividframework.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for binding and validation errors
 * @author sketch
 */
public interface Errors {

    /**
     * Get object name
     */
    String getObjectName();

    /**
     * Get nested path
     */
    String getNestedPath();

    /**
     * Push nested path
     */
    void pushNestedPath(String nestedPath);

    /**
     * Pop nested path
     */
    void popNestedPath();

    /**
     * Reject with error code
     */
    void reject(String errorCode);

    /**
     * Reject with error code and default message
     */
    void reject(String errorCode, String defaultMessage);

    /**
     * Reject with error code, args, and default message
     */
    void reject(String errorCode, Object[] args, String defaultMessage);

    /**
     * Reject field with error code
     */
    void rejectValue(String field, String errorCode);

    /**
     * Reject field with error code and default message
     */
    void rejectValue(String field, String errorCode, String defaultMessage);

    /**
     * Reject field with error code, args, and default message
     */
    void rejectValue(String field, String errorCode, Object[] args, String defaultMessage);

    /**
     * Add error
     */
    void addError(ObjectError error);

    /**
     * Get all errors
     */
    List<ObjectError> getAllErrors();

    /**
     * Get global errors
     */
    List<ObjectError> getGlobalErrors();

    /**
     * Get field errors
     */
    List<FieldError> getFieldErrors();

    /**
     * Check if has errors
     */
    boolean hasErrors();

    /**
     * Check if has global errors
     */
    boolean hasGlobalErrors();

    /**
     * Check if has field errors
     */
    boolean hasFieldErrors();

    /**
     * Check if has error for field
     */
    boolean hasFieldErrors(String field);

    /**
     * Get error count
     */
    int getErrorCount();

    /**
     * Get field error count
     */
    int getFieldErrorCount();

    /**
     * Get global error count
     */
    int getGlobalErrorCount();

    /**
     * Get first error message
     */
    String getFirstError();

    /**
     * Object error implementation
     */
    class ObjectError {
        private final String objectName;
        private final String defaultMessage;
        private final String[] codes;
        private final Object[] args;

        public ObjectError(String objectName, String defaultMessage) {
            this(objectName, null, new Object[0], defaultMessage);
        }

        public ObjectError(String objectName, String[] codes, Object[] args, String defaultMessage) {
            this.objectName = objectName;
            this.defaultMessage = defaultMessage;
            this.codes = codes;
            this.args = args;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }

        public String[] getCodes() {
            return codes;
        }

        public Object[] getArgs() {
            return args;
        }

        @Override
        public String toString() {
            return "ObjectError{" +
                    "objectName='" + objectName + '\'' +
                    ", defaultMessage='" + defaultMessage + '\'' +
                    '}';
        }
    }

    /**
     * Field error implementation
     */
    class FieldError extends ObjectError {
        private final String field;
        private final Object rejectedValue;
        private final boolean bindingFailure;

        public FieldError(String objectName, String field, Object rejectedValue, boolean bindingFailure,
                          String defaultMessage) {
            super(objectName, defaultMessage);
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.bindingFailure = bindingFailure;
        }

        public FieldError(String objectName, String field, Object rejectedValue, boolean bindingFailure,
                          String[] codes, Object[] args, String defaultMessage) {
            super(objectName, codes, args, defaultMessage);
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.bindingFailure = bindingFailure;
        }

        public String getField() {
            return field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public boolean isBindingFailure() {
            return bindingFailure;
        }

        @Override
        public String toString() {
            return "FieldError{" +
                    "field='" + field + '\'' +
                    ", rejectedValue=" + rejectedValue +
                    ", bindingFailure=" + bindingFailure +
                    '}';
        }
    }

    /**
     * Simple errors implementation
     */
    class SimpleErrors implements Errors {
        private final String objectName;
        private String nestedPath = "";
        private final List<ObjectError> errors = new ArrayList<>();

        public SimpleErrors(String objectName) {
            this.objectName = objectName;
        }

        @Override
        public String getObjectName() {
            return objectName;
        }

        @Override
        public String getNestedPath() {
            return nestedPath;
        }

        @Override
        public void pushNestedPath(String nestedPath) {
            this.nestedPath = (this.nestedPath == null || this.nestedPath.isEmpty())
                    ? nestedPath : this.nestedPath + nestedPath;
        }

        @Override
        public void popNestedPath() {
            int lastDot = nestedPath.lastIndexOf('.');
            this.nestedPath = lastDot > 0 ? nestedPath.substring(0, lastDot) : "";
        }

        @Override
        public void reject(String errorCode) {
            errors.add(new ObjectError(objectName, errorCode));
        }

        @Override
        public void reject(String errorCode, String defaultMessage) {
            errors.add(new ObjectError(objectName, new String[]{errorCode}, new Object[0], defaultMessage));
        }

        @Override
        public void reject(String errorCode, Object[] args, String defaultMessage) {
            errors.add(new ObjectError(objectName, new String[]{errorCode}, args, defaultMessage));
        }

        @Override
        public void rejectValue(String field, String errorCode) {
            String fullField = (nestedPath == null || nestedPath.isEmpty()) ? field : nestedPath + "." + field;
            errors.add(new FieldError(objectName, fullField, null, false, errorCode));
        }

        @Override
        public void rejectValue(String field, String errorCode, String defaultMessage) {
            String fullField = (nestedPath == null || nestedPath.isEmpty()) ? field : nestedPath + "." + field;
            errors.add(new FieldError(objectName, fullField, null, false, new String[]{errorCode}, new Object[0], defaultMessage));
        }

        @Override
        public void rejectValue(String field, String errorCode, Object[] args, String defaultMessage) {
            String fullField = (nestedPath == null || nestedPath.isEmpty()) ? field : nestedPath + "." + field;
            errors.add(new FieldError(objectName, fullField, null, false, new String[]{errorCode}, args, defaultMessage));
        }

        @Override
        public void addError(ObjectError error) {
            errors.add(error);
        }

        @Override
        public List<ObjectError> getAllErrors() {
            return new ArrayList<>(errors);
        }

        @Override
        public List<ObjectError> getGlobalErrors() {
            List<ObjectError> result = new ArrayList<>();
            for (ObjectError error : errors) {
                if (error instanceof FieldError) {
                    // skip
                } else {
                    result.add(error);
                }
            }
            return result;
        }

        @Override
        public List<FieldError> getFieldErrors() {
            List<FieldError> result = new ArrayList<>();
            for (ObjectError error : errors) {
                if (error instanceof FieldError) {
                    result.add((FieldError) error);
                }
            }
            return result;
        }

        @Override
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        @Override
        public boolean hasGlobalErrors() {
            return !getGlobalErrors().isEmpty();
        }

        @Override
        public boolean hasFieldErrors() {
            return !getFieldErrors().isEmpty();
        }

        @Override
        public boolean hasFieldErrors(String field) {
            for (FieldError error : getFieldErrors()) {
                if (error.getField().equals(field)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getErrorCount() {
            return errors.size();
        }

        @Override
        public int getFieldErrorCount() {
            return getFieldErrors().size();
        }

        @Override
        public int getGlobalErrorCount() {
            return getGlobalErrors().size();
        }

        @Override
        public String getFirstError() {
            return hasErrors() ? errors.get(0).getDefaultMessage() : null;
        }
    }
}
