package org.vividframework.view;

import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpStatus;
import org.vividframework.http.server.HttpServerRequest;

import java.util.Map;

/**
 * Redirect view
 * @author Jon Fisher
 */
public class RedirectView extends View.AbstractView {

    private final String url;
    private boolean contextRelative = true;
    private boolean http10Compatible = true;
    private boolean exposeModelAttributes = true;

    public RedirectView(String url) {
        this.url = url;
        setContentType("text/html;charset=UTF-8");
    }

    public RedirectView(String url, boolean contextRelative, boolean http10Compatible) {
        this.url = url;
        this.contextRelative = contextRelative;
        this.http10Compatible = http10Compatible;
        setContentType("text/html;charset=UTF-8");
    }

    @Override
    protected void doRender(Map<String, ?> model, HttpServerRequest request, HttpServletResponse response) throws Exception {
        String targetUrl = createTargetUrl(model, request);
        targetUrl = appendQueryProperties(targetUrl, model);

        if (http10Compatible) {
            response.setStatus(HttpStatus.MOVED_PERMANENTLY);
        } else {
            response.setStatus(HttpStatus.SEE_OTHER);
        }
        response.setHeader(HttpHeaders.LOCATION, targetUrl);
        response.setContent(("Redirecting to " + targetUrl).getBytes());
    }

    protected String createTargetUrl(Map<String, ?> model, HttpServerRequest request) {
        String url = this.url;
        if (contextRelative && url.startsWith("/")) {
            String contextPath = request.getPath();
            if (contextPath != null && contextPath.length() > 1) {
                int slashIndex = contextPath.indexOf("/", 1);
                if (slashIndex > 0) {
                    url = contextPath.substring(0, slashIndex) + url;
                } else {
                    url = contextPath + url;
                }
            }
        }
        return url;
    }

    protected String appendQueryProperties(String targetUrl, Map<String, ?> model) {
        if (!exposeModelAttributes) {
            return targetUrl;
        }

        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, ?> entry : model.entrySet()) {
            String name = entry.getKey();
            if (name.startsWith("org.springframework")) {
                continue;
            }
            Object value = entry.getValue();
            if (value != null) {
                if (first) {
                    query.append("?");
                    first = false;
                } else {
                    query.append("&");
                }
                query.append(name).append("=").append(value.toString());
            }
        }

        return targetUrl + query.toString();
    }

    public String getUrl() {
        return url;
    }

    public boolean isContextRelative() {
        return contextRelative;
    }

    public void setContextRelative(boolean contextRelative) {
        this.contextRelative = contextRelative;
    }

    public boolean isHttp10Compatible() {
        return http10Compatible;
    }

    public void setHttp10Compatible(boolean http10Compatible) {
        this.http10Compatible = http10Compatible;
    }

    public boolean isExposeModelAttributes() {
        return exposeModelAttributes;
    }

    public void setExposeModelAttributes(boolean exposeModelAttributes) {
        this.exposeModelAttributes = exposeModelAttributes;
    }
}
