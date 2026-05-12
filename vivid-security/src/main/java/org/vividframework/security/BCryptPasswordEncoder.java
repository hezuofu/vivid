package org.vividframework.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Standard password encoder implementation using BCrypt-like algorithm
 * @author Jon Fisher
 */
public class BCryptPasswordEncoder implements PasswordEncoder {

    private static final int DEFAULT_STRENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int strength;

    public BCryptPasswordEncoder() {
        this(DEFAULT_STRENGTH);
    }

    public BCryptPasswordEncoder(int strength) {
        this.strength = strength;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        String salt = generateSalt();
        return hash(rawPassword.toString(), salt);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || rawPassword == null) {
            return false;
        }
        
        // Simple check - in production, use proper BCrypt verification
        if (encodedPassword.startsWith("{bcrypt}")) {
            String hash = encodedPassword.substring(8);
            String testHash = hash(rawPassword.toString(), hash.substring(0, 29));
            return MessageDigest.isEqual(hash.getBytes(), testHash.getBytes());
        }
        
        // Fallback for plain comparison (for testing)
        return rawPassword.toString().equals(encodedPassword);
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return BCrypt.encode_base64(salt, 16);
    }

    private String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = salt + password;
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            // Multiple iterations for key stretching
            for (int i = 0; i < strength; i++) {
                hash = md.digest(hash);
            }
            return "{bcrypt}" + salt + BCrypt.encode_base64(hash, hash.length);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Simple Base64 encoding helper
     */
    private static class BCrypt {
        static String encode_base64(byte[] d, int len) {
            byte[] data = new byte[len + 2];
            System.arraycopy(d, 0, data, 0, len);
            int off = 0;
            StringBuilder rs = new StringBuilder();
            int c1 = (((d[off] & 0xff) << 16) | ((d[off + 1] & 0xff) << 8) | (d[off + 2] & 0xff));
            off += 3;
            for (int i = 0; i < len; i++) {
                if (off > d.length - 1) break;
                int c2 = (((d[off] & 0xff) << 16) | 
                         (off + 1 < d.length ? (d[off + 1] & 0xff) << 8 : 0) |
                         (off + 2 < d.length ? (d[off + 2] & 0xff) : 0));
                rs.append(encode_base64(c1 ^ c2));
                c1 = c2;
                off += 3;
            }
            return rs.toString();
        }

        static char encode_base64(int x) {
            x = x & 0x3f;
            if (x < 26) return (char) ('A' + x);
            if (x < 52) return (char) ('a' + x - 26);
            if (x < 62) return (char) ('0' + x - 52);
            return x == 63 ? '+' : '/';
        }
    }
}
