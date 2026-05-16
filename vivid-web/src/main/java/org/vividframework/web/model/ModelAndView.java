package org.vividframework.web.model;

/**
 * Holder for model and view information
 * @author sketch
 */
public class ModelAndView {

    private Object view;
    private ModelMap model;
    private boolean cleared = false;

    public ModelAndView() {
    }

    public ModelAndView(String viewName) {
        this.view = viewName;
    }

    public ModelAndView(String viewName, ModelMap model) {
        this.view = viewName;
        this.model = model;
    }

    public ModelAndView(Object view) {
        this.view = view;
    }

    public ModelAndView(String viewName, String modelName, Object modelValue) {
        this.view = viewName;
        this.model = new ExtendedModelMap();
        this.model.addAttribute(modelName, modelValue);
    }

    public String getViewName() {
        return view instanceof String ? (String) view : null;
    }

    public Object getView() {
        return view;
    }

    public ModelMap getModel() {
        if (model == null) {
            model = new ExtendedModelMap();
        }
        return model;
    }

    public void setViewName(String viewName) {
        this.view = viewName;
    }

    public void setView(Object view) {
        this.view = view;
    }

    public void setModel(ModelMap model) {
        this.model = model;
    }

    public boolean hasView() {
        return view != null;
    }

    public boolean isEmpty() {
        return (model == null || model.isEmpty()) && !hasView();
    }

    public boolean hasModel() {
        return model != null && !model.isEmpty();
    }

    public void clear() {
        this.view = null;
        this.model = null;
        this.cleared = true;
    }

    public boolean isCleared() {
        return cleared;
    }

    public ModelAndView addObject(Object attributeValue) {
        getModel().addAttribute(attributeValue);
        return this;
    }

    public ModelAndView addObject(String attributeName, Object attributeValue) {
        getModel().addAttribute(attributeName, attributeValue);
        return this;
    }

    public ModelAndView addAllObjects(java.util.Map<String, ?> attributes) {
        getModel().addAllAttributes(attributes);
        return this;
    }

    public static ModelAndView of(String viewName) {
        return new ModelAndView(viewName);
    }

    public static ModelAndView of(String viewName, ModelMap model) {
        return new ModelAndView(viewName, model);
    }

    public static ModelAndView of(Object view) {
        return new ModelAndView(view);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ModelAndView {");
        if (view != null) {
            sb.append("view=").append(view);
        }
        if (model != null && !model.isEmpty()) {
            sb.append(", model=").append(model);
        }
        sb.append("}");
        return sb.toString();
    }
}
