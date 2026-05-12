package org.vividframework.view;

import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.MediaType;
import org.vividframework.http.server.HttpServerRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JSON view for rendering model as JSON
 * @author Jon Fisher
 */
public class JsonView extends View.AbstractView {

    private static final String DEFAULT_CONTENT_TYPE = "application/json;charset=UTF-8";

    private boolean prefixJson = false;
    private boolean prettyPrint = false;

    public JsonView() {
        setContentType(DEFAULT_CONTENT_TYPE);
    }

    @Override
    protected void doRender(Map<String, ?> model, HttpServerRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());

        if (model == null || model.isEmpty()) {
            response.setContent("{}".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String json = convertToJson(model);
        response.setContent(json.getBytes(StandardCharsets.UTF_8));
    }

    protected String convertToJson(Map<String, ?> model) throws Exception {
        // Use simple JSON conversion for now
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int count = 0;
        for (Map.Entry<String, ?> entry : model.entrySet()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(value.toString()).append("\"");
            }
            count++;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public boolean isPrefixJson() {
        return prefixJson;
    }

    public void setPrefixJson(boolean prefixJson) {
        this.prefixJson = prefixJson;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }
}
