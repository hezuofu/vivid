package org.vividframework.validation;

import org.vividframework.binding.MutablePropertyValues;

import java.beans.PropertyEditor;
import java.util.Map;

/**
 * Data binder for binding request parameters to object
 * @author Jon Fisher
 */
public class DataBinder {

    private final Object target;
    private final String objectName;
    private MutablePropertyValues propertyValues;
    private Errors errors;
    private boolean autoGrowNestedPaths = true;
    private boolean ignoreUnknownFields = true;
    private boolean ignoreInvalidFields = false;

    public DataBinder(Object target) {
        this(target, null);
    }

    public DataBinder(Object target, String objectName) {
        this.target = target;
        this.objectName = objectName != null ? objectName : "target";
    }

    public Object getTarget() {
        return target;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setPropertyValues(MutablePropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }

    public void setPropertyValues(MutablePropertyValues propertyValues, boolean ignoreUnknown) {
        this.propertyValues = propertyValues;
        this.ignoreUnknownFields = ignoreUnknown;
    }

    public void setPropertyValues(Map<String, ?> properties) {
        this.propertyValues = new MutablePropertyValues(properties);
    }

    public void bind() {
        doBind(propertyValues);
    }

    protected void doBind(MutablePropertyValues propertyValues) {
        // Override in subclass
    }

    public Errors getErrors() {
        if (errors == null) {
            errors = new Errors.SimpleErrors(objectName);
        }
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && errors.hasErrors();
    }

    public void close() {
        if (hasErrors()) {
            throw new BindingErrorException(getErrors());
        }
    }

    public void setAutoGrowNestedPaths(boolean autoGrow) {
        this.autoGrowNestedPaths = autoGrow;
    }

    public void setIgnoreUnknownFields(boolean ignore) {
        this.ignoreUnknownFields = ignore;
    }

    public void setIgnoreInvalidFields(boolean ignore) {
        this.ignoreInvalidFields = ignore;
    }

    public static class BindingErrorException extends RuntimeException {
        private final Errors errors;

        public BindingErrorException(Errors errors) {
            super("Binding errors: " + errors.getErrorCount());
            this.errors = errors;
        }

        public Errors getErrors() {
            return errors;
        }
    }
}
