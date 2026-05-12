package org.vividframework.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple password encoder using SHA-256
 * @author Jon Fisher
 */
public class SimplePasswordEncoder implements PasswordEncoder {

    private final String algorithm;

    public SimplePasswordEncoder() {
        this("SHA-256");
    }

    public SimplePasswordEncoder(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return hash(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || rawPassword == null) {
            return false;
        }
        String encoded = hash(rawPassword.toString());
        return MessageDigest.isEqual(
            encoded.getBytes(StandardCharsets.UTF_8),
            encodedPassword.getBytes(StandardCharsets.UTF_8));
    }

    private String hash(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
