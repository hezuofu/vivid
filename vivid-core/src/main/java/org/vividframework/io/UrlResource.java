package org.vividframework.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Resource implementation for URL resources
 * @author Jon Fisher
 */
public class UrlResource implements Resource {

    private final URL url;
    private final URI uri;

    public UrlResource(URL url) {
        this.url = url;
        this.uri = null;
    }

    public UrlResource(URI uri) {
        this.uri = uri;
        this.url = null;
    }

    public UrlResource(String urlPath) throws IOException {
        this.url = new URL(urlPath);
        this.uri = null;
    }

    @Override
    public boolean exists() {
        try {
            return getURL() != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isReadable() {
        return exists();
    }

    @Override
    public URL getURL() throws IOException {
        if (url != null) {
            return url;
        }
        if (uri != null) {
            return uri.toURL();
        }
        return null;
    }

    @Override
    public URI getURI() throws IOException {
        if (uri != null) {
            return uri;
        }
        if (url != null) {
            try {
                return url.toURI();
            } catch (java.net.URISyntaxException e) {
                throw new IOException("Failed to convert URL to URI", e);
            }
        }
        return null;
    }

    @Override
    public File getFile() throws IOException {
        return new File(getURI());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getURL().openStream();
    }

    @Override
    public String getDescription() {
        try {
            return "URL resource [" + getURL() + "]";
        } catch (IOException e) {
            return "URL resource [unavailable]";
        }
    }

    @Override
    public String getFilename() {
        URL urlToUse = null;
        try {
            urlToUse = getURL();
        } catch (IOException e) {
            // Ignore
        }
        if (urlToUse == null) {
            return null;
        }
        String path = urlToUse.getPath();
        int lastSlash = path.lastIndexOf('/');
        return (lastSlash != -1) ? path.substring(lastSlash + 1) : path;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return new UrlResource(new URL(getURL(), relativePath));
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UrlResource)) {
            return false;
        }
        UrlResource other = (UrlResource) obj;
        try {
            return getURL().equals(other.getURL());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return getURL().hashCode();
        } catch (IOException e) {
            return 0;
        }
    }
}
