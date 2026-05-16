package org.vividframework.http;

import java.util.Arrays;
import java.util.List;

/**
 * HTTP Media Types
 * @author sketch
 */
public final class MediaType implements Comparable<MediaType> {

    public static final MediaType ALL = new MediaType("*", "*");
    public static final MediaType ALL_VALUE = new MediaType("*", "*", 1.0);
    
    public static final MediaType APPLICATION_JSON = new MediaType("application", "json");
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType("application", "json", "utf-8");
    public static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");
    public static final MediaType APPLICATION_FORM_URLENCODED = new MediaType("application", "x-www-form-urlencoded");
    public static final MediaType APPLICATION_MULTIPART_FORM_DATA = new MediaType("multipart", "form-data");
    public static final MediaType APPLICATION_XML = new MediaType("application", "xml");
    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain");
    public static final MediaType TEXT_HTML = new MediaType("text", "html");
    public static final MediaType TEXT_XML = new MediaType("text", "xml");

    private final String type;
    private final String subtype;
    private final String charset;
    private final double qualityValue;

    public MediaType(String type, String subtype) {
        this(type, subtype, null);
    }

    public MediaType(String type, String subtype, String charset) {
        this(type, subtype, charset, 1.0);
    }

    public MediaType(String type, String subtype, double qualityValue) {
        this(type, subtype, null, qualityValue);
    }

    public MediaType(String type, String subtype, String charset, double qualityValue) {
        this.type = type.toLowerCase();
        this.subtype = subtype.toLowerCase();
        this.charset = charset;
        this.qualityValue = qualityValue;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getCharset() {
        return charset;
    }

    public double getQualityValue() {
        return qualityValue;
    }

    public boolean isAllTypes() {
        return "*".equals(type);
    }

    public boolean isAllSubTypes() {
        return "*".equals(subtype);
    }

    public boolean isConcrete() {
        return !isAllTypes() && !isAllSubTypes();
    }

    public boolean isCompatibleWith(MediaType other) {
        return (isAllTypes() || other.isAllTypes() || type.equals(other.type)) &&
               (isAllSubTypes() || other.isAllSubTypes() || subtype.equals(other.subtype));
    }

    public boolean isPresentIn(List<MediaType> mediaTypes) {
        for (MediaType mediaType : mediaTypes) {
            if (includes(mediaType)) {
                return true;
            }
        }
        return false;
    }

    public boolean includes(MediaType other) {
        return (isAllTypes() || type.equals(other.type)) &&
               (isAllSubTypes() || subtype.equals(other.subtype));
    }

    @Override
    public int compareTo(MediaType other) {
        int cmp = subtype.compareTo(other.subtype);
        if (cmp != 0) {
            return cmp;
        }
        cmp = type.compareTo(other.type);
        if (cmp != 0) {
            return cmp;
        }
        if (qualityValue < other.qualityValue) return 1;
        if (qualityValue > other.qualityValue) return -1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaType mediaType = (MediaType) o;
        return Double.compare(mediaType.qualityValue, qualityValue) == 0 &&
               type.equals(mediaType.type) &&
               subtype.equals(mediaType.subtype) &&
               charset != null ? charset.equals(mediaType.charset) : mediaType.charset == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + subtype.hashCode();
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        result = 31 * result + Double.hashCode(qualityValue);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append('/').append(subtype);
        if (charset != null) {
            sb.append(";charset=").append(charset);
        }
        if (qualityValue < 1.0) {
            sb.append(";q=").append(qualityValue);
        }
        return sb.toString();
    }

    public static MediaType parse(String value) {
        if (value == null || value.isEmpty()) {
            return ALL;
        }
        String[] parts = value.split(";");
        String[] typeSubtype = parts[0].split("/");
        if (typeSubtype.length != 2) {
            return ALL;
        }
        String type = typeSubtype[0].trim();
        String subtype = typeSubtype[1].trim();
        String charset = null;
        double quality = 1.0;
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.startsWith("charset=")) {
                charset = part.substring(8).trim();
            } else if (part.startsWith("q=")) {
                try {
                    quality = Double.parseDouble(part.substring(2).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return new MediaType(type, subtype, charset, quality);
    }

    public static List<MediaType> parseMediaTypes(String... values) {
        if (values == null || values.length == 0) {
            return Arrays.asList(ALL);
        }
        MediaType[] result = new MediaType[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = parse(values[i]);
        }
        return Arrays.asList(result);
    }
}
