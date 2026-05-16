package org.vividframework.web.converter;

import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.MediaType;
import org.vividframework.http.HttpServerRequest;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * HTTP message converter for String
 * @author Jon Fisher
 */
public class StringHttpMessageConverter extends HttpMessageConverter.AbstractHttpMessageConverter<String> {

    private Charset defaultCharset = StandardCharsets.UTF_8;

    public StringHttpMessageConverter() {
        super(MediaType.TEXT_PLAIN, new MediaType[0]);
    }

    public StringHttpMessageConverter(Charset defaultCharset) {
        super(MediaType.TEXT_PLAIN, new MediaType[0]);
        this.defaultCharset = defaultCharset;
    }

    @Override
    protected boolean canReadClass(Class<?> clazz) {
        return CharSequence.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
    }

    @Override
    protected boolean canWriteClass(Class<?> clazz) {
        return CharSequence.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
    }

    @Override
    public String read(Class<? extends String> clazz, HttpServerRequest request) throws Exception {
        MediaType contentType = getContentType(request);
        Charset charset = getCharset(contentType);
        String body = request.getBodyAsString(charset);
        return body;
    }

    @Override
    public void write(String object, MediaType contentType, HttpServletResponse response) throws Exception {
        Charset charset = getCharset(response.getContentType());
        String contentTypeStr = contentType != null ? contentType.toString() : "text/plain;charset=" + charset.name();
        byte[] bytes = object.getBytes(charset);
        response.setContentType(contentTypeStr);
        response.setContent(bytes);
    }

    private MediaType getContentType(HttpServerRequest request) {
        MediaType contentType = request.getContentType();
        return contentType != null ? contentType : MediaType.TEXT_PLAIN;
    }

    private Charset getCharset(MediaType contentType) {
        if (contentType != null && contentType.getCharset() != null) {
            return Charset.forName(contentType.getCharset());
        }
        return defaultCharset;
    }

    public void setDefaultCharset(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }
}
