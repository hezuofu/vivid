package org.vividframework.web.view;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;

import java.util.Map;

/**
 * HTML view with simple template rendering.
 * Supports {@code {{key}}} placeholders that are replaced with model values.
 * @author sketch
 */
public class HtmlView extends View.AbstractView {

    private final String template;

    public HtmlView(String template) {
        this.template = template;
        setContentType("text/html;charset=UTF-8");
    }

    public HtmlView(String template, String contentType) {
        this.template = template;
        setContentType(contentType);
    }

    @Override
    protected void doRender(Map<String, ?> model, HttpServerRequest request,
                            HttpServletResponse.Builder builder) throws Exception {
        String html = template;
        if (model != null && !model.isEmpty()) {
            html = renderTemplate(html, model);
        }
        builder.html(html);
    }

    /**
     * Simple template engine: replaces {{key}} with model values.
     */
    protected String renderTemplate(String template, Map<String, ?> model) {
        String result = template;
        for (Map.Entry<String, ?> entry : model.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    public static HtmlView of(String html) {
        return new HtmlView(html);
    }
}
