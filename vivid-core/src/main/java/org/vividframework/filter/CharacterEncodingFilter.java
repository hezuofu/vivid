package org.vividframework.filter;

import org.vividframework.http.HttpHeaders;
import org.vividframework.http.MediaType;
import org.vividframework.http.server.HttpServerRequest;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Filter for setting character encoding
 * @author Jon Fisher
 */
public class CharacterEncodingFilter extends OncePerRequestFilter {

    private Charset encoding = StandardCharsets.UTF_8;
    private boolean forceRequestEncoding = true;
    private boolean forceResponseEncoding = true;

    public CharacterEncodingFilter() {
    }

    public CharacterEncodingFilter(String encoding) {
        this(encoding, true, true);
    }

    public CharacterEncodingFilter(String encoding, boolean forceRequestEncoding, boolean forceResponseEncoding) {
        this.encoding = Charset.forName(encoding);
        this.forceRequestEncoding = forceRequestEncoding;
        this.forceResponseEncoding = forceResponseEncoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = Charset.forName(encoding);
    }

    public void setForceRequestEncoding(boolean forceRequestEncoding) {
        this.forceRequestEncoding = forceRequestEncoding;
    }

    public void setForceResponseEncoding(boolean forceResponseEncoding) {
        this.forceResponseEncoding = forceResponseEncoding;
    }

    @Override
    protected void doFilterInternal(HttpServerRequest request, FilterChain chain) throws Exception {
        String encoding = this.encoding.name();

        if (forceRequestEncoding) {
            String existingEncoding = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (existingEncoding == null || !existingEncoding.contains("charset=")) {
                HttpHeaders headers = request.getHeaders();
                String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
                if (contentType != null) {
                    headers.set(HttpHeaders.CONTENT_TYPE, contentType + ";charset=" + encoding);
                }
            }
        }

        if (forceResponseEncoding) {
            // Response encoding will be handled by the response writer
        }

        chain.doFilter(request);
    }
}
