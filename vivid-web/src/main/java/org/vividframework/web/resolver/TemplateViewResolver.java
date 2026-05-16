package org.vividframework.web.resolver;

import org.vividframework.io.ClassPathResource;
import org.vividframework.io.Resource;
import org.vividframework.web.view.HtmlView;
import org.vividframework.web.view.TextView;
import org.vividframework.web.view.View;
import org.vividframework.web.view.XmlView;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Resolves view names to template files loaded from classpath.
 *
 * Resolution rules:
 * <ul>
 *   <li>{@code html:<name>} → templates/name.html</li>
 *   <li>{@code text:<name>} → templates/name.txt</li>
 *   <li>{@code xml:<name>}  → templates/name.xml</li>
 *   <li>{@code <name>}      → templates/name.html (default)</li>
 * </ul>
 *
 * @author Jon Fisher
 */
public class TemplateViewResolver implements ViewResolver {

    private static final String DEFAULT_PREFIX = "templates/";
    private static final String DEFAULT_SUFFIX = ".html";

    private String prefix = DEFAULT_PREFIX;
    private String suffix = DEFAULT_SUFFIX;
    private String encoding = "UTF-8";

    public TemplateViewResolver() {}

    public TemplateViewResolver(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public View resolveViewName(String viewName) throws Exception {
        if (viewName == null) return null;

        String name = viewName;
        String type = "html";

        // Check for type prefix
        if (name.startsWith("html:")) {
            type = "html";
            name = name.substring(5);
        } else if (name.startsWith("text:")) {
            type = "text";
            name = name.substring(5);
        } else if (name.startsWith("xml:")) {
            type = "xml";
            name = name.substring(4);
        } else if (name.startsWith("json:")) {
            return null; // handled by JsonViewResolver
        } else if (name.startsWith("redirect:")) {
            return null; // handled by RedirectViewResolver
        }

        String path = prefix + name + "." + type;
        Resource resource = loadTemplate(path);

        if (resource == null) {
            return null;
        }

        String content = readToString(resource);
        return switch (type) {
            case "text" -> new TextView(content);
            case "xml" -> new XmlView(content);
            default -> new HtmlView(content);
        };
    }

    protected Resource loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                return resource;
            }
        } catch (Exception ignored) {}
        return null;
    }

    protected String readToString(Resource resource) throws Exception {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void setPrefix(String prefix) { this.prefix = prefix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
}
