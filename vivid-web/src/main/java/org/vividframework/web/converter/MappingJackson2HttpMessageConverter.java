package org.vividframework.web.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.MediaType;
import org.vividframework.http.HttpServerRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP message converter using Jackson for JSON
 * @author sketch
 */
public class MappingJackson2HttpMessageConverter extends HttpMessageConverter.AbstractHttpMessageConverter<Object> {

    private ObjectMapper objectMapper;
    private boolean prettyPrint = false;

    public MappingJackson2HttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, new MediaType[0]);
        initObjectMapper();
    }

    private void initObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    protected boolean canReadClass(Class<?> clazz) {
        return !clazz.isPrimitive() && !clazz.isArray();
    }

    @Override
    protected boolean canWriteClass(Class<?> clazz) {
        return true;
    }

    @Override
    public Object read(Class<? extends Object> clazz, HttpServerRequest request) throws Exception {
        String body = request.getBodyAsString(StandardCharsets.UTF_8);
        if (body == null || body.isEmpty()) {
            return null;
        }
        JsonParser parser = objectMapper.getFactory().createParser(body);
        return objectMapper.readValue(parser, clazz);
    }

    @Override
    public void write(Object object, MediaType contentType, HttpServletResponse response) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        if (prettyPrint) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        JsonGenerator generator = objectMapper.getFactory().createGenerator(baos);
        objectMapper.writeValue(generator, object);
        generator.flush();
        
        byte[] jsonBytes = baos.toByteArray();
        response.setContentType(contentType != null ? contentType.toString() : "application/json");
        response.setContent(jsonBytes);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * Write value directly to output stream
     */
    public void writeValue(Object value, java.io.OutputStream out) throws IOException {
        objectMapper.writeValue(out, value);
    }

    /**
     * Read value directly from input stream
     */
    public <T> T readValue(java.io.InputStream inputStream, Class<T> valueType) throws IOException {
        return objectMapper.readValue(inputStream, valueType);
    }

    /**
     * Convert value to JSON string
     */
    public String writeValueAsString(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
}
