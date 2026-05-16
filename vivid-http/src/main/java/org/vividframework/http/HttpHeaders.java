package org.vividframework.http;

import java.util.*;

/**
 * Immutable HTTP headers implementation similar to Spring's HttpHeaders
 * @author sketch
 */
public final class HttpHeaders implements Iterable<String> {

    // Common HTTP header names
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String ACCEPT_RANGES = "Accept-Ranges";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String AGE = "Age";
    public static final String ALLOW = "Allow";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LANGUAGE = "Content-Language";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_LOCATION = "Content-Location";
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String COOKIE = "Cookie";
    public static final String DATE = "Date";
    public static final String ETAG = "ETag";
    public static final String EXPECT = "Expect";
    public static final String EXPIRES = "Expires";
    public static final String FROM = "From";
    public static final String HOST = "Host";
    public static final String IF_MATCH = "If-Match";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String IF_NONE_MATCH = "If-None-Match";
    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String LOCATION = "Location";
    public static final String MAX_FORWARDS = "Max-Forwards";
    public static final String ORIGIN = "Origin";
    public static final String PRAGMA = "Pragma";
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String RANGE = "Range";
    public static final String REFERER = "Referer";
    public static final String RETRY_AFTER = "Retry-After";
    public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
    public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    public static final String SEC_WEBSOCKET_LOCATION = "Sec-WebSocket-Location";
    public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
    public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
    public static final String SERVER = "Server";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String SET_COOKIE2 = "Set-Cookie2";
    public static final String TE = "TE";
    public static final String TIMING_ALLOW_ORIGIN = "Timing-Allow-Origin";
    public static final String TRAILER = "Trailer";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String UPGRADE = "Upgrade";
    public static final String USER_AGENT = "User-Agent";
    public static final String VARY = "Vary";
    public static final String VIA = "Via";
    public static final String WARNING = "Warning";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    private final Map<String, List<String>> headers;

    public HttpHeaders() {
        this(new LinkedHashMap<>());
    }

    private HttpHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    // ========== Get operations ==========

    public String getFirst(String headerName) {
        List<String> values = headers.get(normalizeHeaderName(headerName));
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    public List<String> get(String headerName) {
        List<String> values = headers.get(normalizeHeaderName(headerName));
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }

    public int getContentLength() {
        String value = getFirst(CONTENT_LENGTH);
        return value != null ? Integer.parseInt(value) : -1;
    }

    public MediaType getContentType() {
        String value = getFirst(CONTENT_TYPE);
        return value != null ? MediaType.parse(value) : null;
    }

    public long getDate() {
        String value = getFirst(DATE);
        return value != null ? parseDate(value) : -1;
    }

    public long getExpires() {
        String value = getFirst(EXPIRES);
        return value != null ? parseDate(value) : -1;
    }

    public long getLastModified() {
        String value = getFirst(LAST_MODIFIED);
        return value != null ? parseDate(value) : -1;
    }

    public long getIfModifiedSince() {
        String value = getFirst(IF_MODIFIED_SINCE);
        return value != null ? parseDate(value) : -1;
    }

    public long getIfUnmodifiedSince() {
        String value = getFirst(IF_UNMODIFIED_SINCE);
        return value != null ? parseDate(value) : -1;
    }

    public List<MediaType> getAccept() {
        String value = getFirst(ACCEPT);
        return value != null ? MediaType.parseMediaTypes(value.split(",")) : Collections.emptyList();
    }

    public List<String> getAcceptCharset() {
        String value = getFirst(ACCEPT_CHARSET);
        return value != null ? Arrays.asList(value.split(",")) : Collections.emptyList();
    }

    public List<String> getAcceptEncoding() {
        String value = getFirst(ACCEPT_ENCODING);
        return value != null ? Arrays.asList(value.split(",")) : Collections.emptyList();
    }

    public List<String> getAcceptLanguage() {
        String value = getFirst(ACCEPT_LANGUAGE);
        return value != null ? Arrays.asList(value.split(",")) : Collections.emptyList();
    }

    public String getOrigin() {
        return getFirst(ORIGIN);
    }

    public String getHost() {
        return getFirst(HOST);
    }

    public String getReferer() {
        return getFirst(REFERER);
    }

    public String getUserAgent() {
        return getFirst(USER_AGENT);
    }

    public String getLocation() {
        return getFirst(LOCATION);
    }

    public String getCacheControl() {
        return getFirst(CACHE_CONTROL);
    }

    // ========== Set operations ==========

    public HttpHeaders set(String headerName, String headerValue) {
        List<String> values = new ArrayList<>();
        if (headerValue != null) {
            values.add(headerValue);
        }
        headers.put(normalizeHeaderName(headerName), values);
        return this;
    }

    public HttpHeaders set(String headerName, List<String> headerValues) {
        headers.put(normalizeHeaderName(headerName), new ArrayList<>(headerValues));
        return this;
    }

    public HttpHeaders setContentLength(long contentLength) {
        set(CONTENT_LENGTH, String.valueOf(contentLength));
        return this;
    }

    public HttpHeaders setContentType(MediaType mediaType) {
        if (mediaType != null) {
            set(CONTENT_TYPE, mediaType.toString());
        } else {
            remove(CONTENT_TYPE);
        }
        return this;
    }

    public HttpHeaders setDate(long date) {
        set(DATE, formatDate(date));
        return this;
    }

    public HttpHeaders setExpires(long expires) {
        set(EXPIRES, formatDate(expires));
        return this;
    }

    public HttpHeaders setLastModified(long lastModified) {
        set(LAST_MODIFIED, formatDate(lastModified));
        return this;
    }

    public HttpHeaders setIfModifiedSince(long ifModifiedSince) {
        set(IF_MODIFIED_SINCE, formatDate(ifModifiedSince));
        return this;
    }

    public HttpHeaders setIfUnmodifiedSince(long ifUnmodifiedSince) {
        set(IF_UNMODIFIED_SINCE, formatDate(ifUnmodifiedSince));
        return this;
    }

    // ========== Add operations ==========

    public HttpHeaders add(String headerName, String headerValue) {
        String normalizedName = normalizeHeaderName(headerName);
        headers.computeIfAbsent(normalizedName, k -> new ArrayList<>()).add(headerValue);
        return this;
    }

    public HttpHeaders addAll(String headerName, List<? extends String> headerValues) {
        List<String> values = headers.computeIfAbsent(normalizeHeaderName(headerName), k -> new ArrayList<>());
        values.addAll(headerValues);
        return this;
    }

    /**
     * Add all headers from another HttpHeaders instance.
     */
    public HttpHeaders addAll(HttpHeaders other) {
        for (String name : other.keySet()) {
            for (String value : other.get(name)) {
                add(name, value);
            }
        }
        return this;
    }

    public HttpHeaders addContentLength(long contentLength) {
        add(CONTENT_LENGTH, String.valueOf(contentLength));
        return this;
    }

    // ========== Remove and clear ==========

    public HttpHeaders remove(String headerName) {
        headers.remove(normalizeHeaderName(headerName));
        return this;
    }

    public void clear() {
        headers.clear();
    }

    // ========== Check operations ==========

    public boolean containsKey(String headerName) {
        return headers.containsKey(normalizeHeaderName(headerName));
    }

    public boolean containsValue(String headerValue) {
        return headers.containsValue(headerValue);
    }

    public boolean isEmpty() {
        return headers.isEmpty();
    }

    public int size() {
        return headers.size();
    }

    // ========== Bulk access ==========

    public Set<String> keySet() {
        return Collections.unmodifiableSet(headers.keySet());
    }

    public Map<String, List<String>> toMap() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        headers.forEach((key, value) -> result.put(key, Collections.unmodifiableList(value)));
        return Collections.unmodifiableMap(result);
    }

    // ========== Iterator ==========

    @Override
    public Iterator<String> iterator() {
        return keySet().iterator();
    }

    // ========== Static factory ==========

    public static HttpHeaders readOnlyHttpHeaders(HttpHeaders headers) {
        HttpHeaders result = new HttpHeaders(Collections.unmodifiableMap(headers.headers));
        return result;
    }

    // ========== Helper methods ==========

    private String normalizeHeaderName(String name) {
        if (name == null) {
            return null;
        }
        // Common headers - use the constant name
        String lower = name.toLowerCase();
        switch (lower) {
            case "accept": return ACCEPT;
            case "accept-charset": return ACCEPT_CHARSET;
            case "accept-encoding": return ACCEPT_ENCODING;
            case "accept-language": return ACCEPT_LANGUAGE;
            case "authorization": return AUTHORIZATION;
            case "cache-control": return CACHE_CONTROL;
            case "content-type": return CONTENT_TYPE;
            case "content-length": return CONTENT_LENGTH;
            case "cookie": return COOKIE;
            case "date": return DATE;
            case "host": return HOST;
            case "origin": return ORIGIN;
            case "referer": return REFERER;
            case "user-agent": return USER_AGENT;
            case "location": return LOCATION;
            default: return name;
        }
    }

    private long parseDate(String dateValue) {
        // Simple RFC 7231 date parsing
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
            format.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            return format.parse(dateValue).getTime();
        } catch (Exception e) {
            return -1;
        }
    }

    private String formatDate(long date) {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
        format.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        return format.format(new Date(date));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HttpHeaders {");
        headers.forEach((name, values) -> {
            sb.append(name).append("=");
            if (values.size() == 1) {
                sb.append(values.get(0));
            } else {
                sb.append("[").append(String.join(", ", values)).append("]");
            }
            sb.append(", ");
        });
        if (!headers.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return sb.toString();
    }
}
