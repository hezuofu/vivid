package org.vividframework.web.view;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;

import java.util.Map;

/**
 * Plain text view. Renders model values as text.
 * @author Jon Fisher
 */
public class TextView extends View.AbstractView {

    private final String text;

    public TextView() {
        this(null);
    }

    public TextView(String text) {
        this.text = text;
        setContentType("text/plain;charset=UTF-8");
    }

    @Override
    protected void doRender(Map<String, ?> model, HttpServerRequest request,
                            HttpServletResponse.Builder builder) throws Exception {
        if (text != null) {
            builder.text(text);
        } else if (model != null && !model.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, ?> entry : model.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            builder.text(sb.toString());
        } else {
            builder.text("");
        }
    }

    public static TextView of(String text) {
        return new TextView(text);
    }
}
