package org.vividframework.web.view;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;

import java.util.Map;

/**
 * XML view with simple template rendering.
 * Supports {@code {{key}}} placeholders replaced with model values.
 * Auto-wraps bare content in {@code <?xml?>} prolog.
 * @author Jon Fisher
 */
public class XmlView extends View.AbstractView {

    private final String template;

    public XmlView(String template) {
        this.template = template;
        setContentType("application/xml;charset=UTF-8");
    }

    @Override
    protected void doRender(Map<String, ?> model, HttpServerRequest request,
                            HttpServletResponse.Builder builder) throws Exception {
        String xml = template;
        if (model != null && !model.isEmpty()) {
            xml = renderTemplate(xml, model);
        }
        // Auto-add XML prolog if missing
        if (!xml.trim().startsWith("<?xml")) {
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml;
        }
        builder.content(xml);
    }

    protected String renderTemplate(String template, Map<String, ?> model) {
        String result = template;
        for (Map.Entry<String, ?> entry : model.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? escapeXml(entry.getValue().toString()) : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    protected String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static XmlView of(String xml) {
        return new XmlView(xml);
    }
}
