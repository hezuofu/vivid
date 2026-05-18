package org.vividframework.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Parses multipart/form-data requests into file parts and form fields.
 * Lightweight implementation without external dependencies.
 * @author sketch
 */
public class MultipartRequest {

    private final List<MultipartFile> files = new ArrayList<>();
    private final Map<String, String[]> formFields = new LinkedHashMap<>();
    private final Map<String, List<MultipartFile>> fileFields = new LinkedHashMap<>();

    /**
     * Parse a multipart/form-data request body.
     */
    public static MultipartRequest parse(String contentType, byte[] body) {
        MultipartRequest result = new MultipartRequest();
        if (body == null || body.length == 0 || contentType == null) return result;

        String boundary = extractBoundary(contentType);
        if (boundary == null) return result;

        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] endBoundary = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
        byte[] crlf = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        int pos = indexOf(body, boundaryBytes, 0) + boundaryBytes.length;

        while (pos < body.length) {
            // Skip \r\n after boundary
            if (pos + 1 < body.length && body[pos] == '\r' && body[pos + 1] == '\n') pos += 2;

            // Check for end boundary
            if (indexOf(body, endBoundary, pos) == pos) break;

            // Find end of headers (\r\n\r\n)
            int headerEnd = indexOf(body, crlf, pos);
            if (headerEnd < 0) break;

            // Parse headers
            String headers = new String(body, pos, headerEnd - pos, StandardCharsets.UTF_8);
            String name = extractHeader(headers, "name");
            String filename = extractHeader(headers, "filename");
            String partContentType = extractHeader(headers, "Content-Type");

            pos = headerEnd + crlf.length;

            // Find next boundary
            int nextBoundary = indexOf(body, boundaryBytes, pos);
            if (nextBoundary < 0) break;

            // Skip \r\n before boundary
            int dataEnd = nextBoundary - 2;
            if (dataEnd > pos && body[dataEnd] == '\n' && body[dataEnd - 1] == '\r') dataEnd -= 2;

            byte[] partData = Arrays.copyOfRange(body, pos, dataEnd);

            if (filename != null && !filename.isEmpty()) {
                MultipartFile file = new SimpleMultipartFile(name, filename, partContentType, partData);
                result.files.add(file);
                result.fileFields.computeIfAbsent(name, k -> new ArrayList<>()).add(file);
            } else if (name != null) {
                String value = new String(partData, StandardCharsets.UTF_8);
                result.formFields.merge(name, new String[]{value},
                        (old, n) -> {
                            String[] merged = new String[old.length + 1];
                            System.arraycopy(old, 0, merged, 0, old.length);
                            merged[old.length] = value;
                            return merged;
                        });
            }

            pos = dataEnd + (nextBoundary - dataEnd);
        }

        return result;
    }

    public List<MultipartFile> getFiles() { return files; }
    public MultipartFile getFile(String name) {
        List<MultipartFile> list = fileFields.get(name);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }
    public List<MultipartFile> getFiles(String name) {
        return fileFields.getOrDefault(name, List.of());
    }
    public Map<String, String[]> getFormFields() { return formFields; }
    public String getFormField(String name) {
        String[] values = formFields.get(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    // --- Parsing helpers ---

    static String extractBoundary(String contentType) {
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring(9);
            }
        }
        return null;
    }

    static String extractHeader(String headers, String name) {
        for (String line : headers.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-disposition") && name.equals("filename")) {
                int idx = line.indexOf("filename=\"");
                if (idx >= 0) {
                    int end = line.indexOf("\"", idx + 10);
                    if (end >= 0) return line.substring(idx + 10, end);
                }
            }
            if (line.toLowerCase().startsWith("content-disposition") && name.equals("name")) {
                int idx = line.indexOf("name=\"");
                if (idx >= 0) {
                    int end = line.indexOf("\"", idx + 6);
                    if (end >= 0) return line.substring(idx + 6, end);
                }
            }
            if (line.toLowerCase().startsWith("content-type:") && name.equals("Content-Type")) {
                return line.substring(13).trim();
            }
        }
        return null;
    }

    static int indexOf(byte[] data, byte[] pattern, int start) {
        outer:
        for (int i = start; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    // --- Simple implementation ---

    static class SimpleMultipartFile implements MultipartFile {
        private final String fieldName, filename, contentType;
        private final byte[] data;
        SimpleMultipartFile(String fieldName, String filename, String contentType, byte[] data) {
            this.fieldName = fieldName; this.filename = filename;
            this.contentType = contentType; this.data = data;
        }
        @Override public String getName() { return fieldName; }
        @Override public String getOriginalFilename() { return filename; }
        @Override public String getContentType() { return contentType; }
        @Override public long getSize() { return data.length; }
        @Override public byte[] getBytes() { return data; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(data); }
    }
}
