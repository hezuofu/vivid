package org.vividframework.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;

import java.util.Map;

/**
 * JSON view that renders model as JSON using Jackson.
 * @author Jon Fisher
 */
public class JsonView extends View.AbstractView {

    private static final String DEFAULT_CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private boolean prettyPrint = false;

    public JsonView() {
        setContentType(DEFAULT_CONTENT_TYPE);
    }

    @Override
    protected void doRender(Map<String, ?> model, HttpServerRequest request,
                            HttpServletResponse.Builder builder) throws Exception {
        if (model == null || model.isEmpty()) {
            builder.content("{}");
            return;
        }
        String json = prettyPrint
                ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(model)
                : objectMapper.writeValueAsString(model);
        builder.json(json);
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }
}
