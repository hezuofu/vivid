package org.vividframework.util;

import java.util.*;

/**
 * Ant-style path pattern matcher
 * @author sketch
 */
public class AntPathMatcher {

    private String pathSeparator = "/";
    private boolean trimTokens = true;
    private volatile Boolean cacheWildcardPatterns = true;
    private volatile Boolean cachePathPattern = true;

    private final Map<String, String[]> tokenizedPatternCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, AntPathComparator> antPathComparatorCache = new java.util.concurrent.ConcurrentHashMap<>();

    public AntPathMatcher() {
    }

    public AntPathMatcher(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    public boolean isPattern(String pattern) {
        return pattern != null && (pattern.indexOf('*') >= 0 || pattern.indexOf('?') >= 0);
    }

    public boolean match(String pattern, String path) {
        return doMatch(pattern, path, true);
    }

    public boolean matchStart(String pattern, String path) {
        return doMatch(pattern, path, false);
    }

    protected boolean doMatch(String pattern, String path, boolean fullMatch) {
        if (path == null) {
            return false;
        }
        if (pattern == null) {
            return false;
        }

        // Handle exact match or pattern matching
        if (pattern.equals(path)) {
            return true;
        }

        String[] patternParts = tokenizePattern(pattern);
        String[] pathParts = tokenizePath(path);

        int patternIdxStart = 0;
        int patternIdxEnd = patternParts.length - 1;
        int pathIdxStart = 0;
        int pathIdxEnd = pathParts.length - 1;

        // Match leading **
        while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd) {
            String patternPart = patternParts[patternIdxStart];
            if ("**".equals(patternPart)) {
                break;
            }
            if (!matchStrings(patternPart, pathParts[pathIdxStart])) {
                return false;
            }
            patternIdxStart++;
            pathIdxStart++;
        }

        if (pathIdxStart > pathIdxEnd) {
            // Path is exhausted, check if pattern is also done
            if (patternIdxStart > patternIdxEnd) {
                return (pattern.endsWith(pathSeparator) == path.endsWith(pathSeparator));
            }
            if (!fullMatch) {
                return true;
            }
            if (patternIdxStart == patternIdxEnd && patternParts[patternIdxStart].equals("*") && path.isEmpty()) {
                return true;
            }
            for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
                if (!"**".equals(patternParts[i])) {
                    return false;
                }
            }
            return true;
        } else if (patternIdxStart > patternIdxEnd) {
            // Pattern exhausted before path
            return false;
        } else if (!fullMatch && "**".equals(patternParts[patternIdxStart])) {
            // Additional wildcards
            return true;
        }

        // Fit remaining pattern against remaining path
        while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd) {
            String patternPart = patternParts[patternIdxEnd];
            if ("**".equals(patternPart)) {
                break;
            }
            if (!matchStrings(patternPart, pathParts[pathIdxEnd])) {
                return false;
            }
            patternIdxEnd--;
            pathIdxEnd--;
        }

        if (pathIdxStart > pathIdxEnd) {
            // Path exhausted
            for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
                if (!"**".equals(patternParts[i])) {
                    return false;
                }
            }
            return true;
        }

        while (patternIdxStart != patternIdxEnd && pathIdxStart <= pathIdxEnd) {
            int patIdxLoop = -1;
            for (int i = patternIdxStart + 1; i <= patternIdxEnd; i++) {
                if ("**".equals(patternParts[i])) {
                    patIdxLoop = i;
                    break;
                }
            }
            if (patIdxLoop == patternIdxStart + 1) {
                // **/**, consume one path part
                patternIdxStart++;
                continue;
            }

            int patternLength = patIdxLoop - patternIdxStart - 1;
            int pathLength = pathIdxEnd - pathIdxStart + 1;
            int foundPos = -1;

            for (int i = 0; i <= pathLength - patternLength; i++) {
                boolean failed = false;
                for (int j = 0; j < patternLength; j++) {
                    String pStr = patternParts[patternIdxStart + j];
                    String sStr = pathParts[pathIdxStart + i + j];
                    if (!matchStrings(pStr, sStr)) {
                        failed = true;
                        break;
                    }
                }
                if (!failed) {
                    foundPos = pathIdxStart + i;
                    break;
                }
            }

            if (foundPos == -1) {
                return false;
            }

            patternIdxStart = patIdxLoop;
            pathIdxStart = foundPos + patternLength;
        }

        for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
            if (!"**".equals(patternParts[i])) {
                return false;
            }
        }

        return true;
    }

    private String[] tokenizePattern(String pattern) {
        String[] cached = tokenizedPatternCache.get(pattern);
        if (cached != null) {
            return cached;
        }
        String[] tokens = tokenizePath(pattern);
        tokenizedPatternCache.put(pattern, tokens);
        return tokens;
    }

    private String[] tokenizePath(String path) {
        if (trimTokens) {
            path = path.trim();
        }
        String[] parts = path.split(pathSeparator);
        if (trimTokens) {
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
        }
        return parts;
    }

    private boolean matchStrings(String pattern, String str) {
        if (pattern == null) {
            return str == null;
        }
        if (str == null) {
            return false;
        }

        int wildcardIndex = pattern.indexOf('*');
        if (wildcardIndex < 0) {
            return pattern.equals(str);
        }

        if (wildcardIndex == 0) {
            // Starts with *
            return str.endsWith(pattern.substring(1));
        }
        if (wildcardIndex == pattern.length() - 1) {
            // Ends with *
            return str.startsWith(pattern.substring(0, pattern.length() - 1));
        }

        // Contains *
        String before = pattern.substring(0, wildcardIndex);
        String after = pattern.substring(wildcardIndex + 1);
        int strIndex = str.indexOf(before);
        if (strIndex < 0) {
            return false;
        }
        return str.substring(strIndex + before.length()).startsWith(after);
    }

    public String[] extractPathWithinPattern(String pattern, String path) {
        String[] patternParts = tokenizePattern(pattern);
        String[] pathParts = tokenizePath(path);
        List<String> result = new ArrayList<>();

        int patternIdxStart = 0;
        while (patternIdxStart < patternParts.length && !isDoubleWildcard(patternParts[patternIdxStart])) {
            patternIdxStart++;
        }

        int patternIdxEnd = patternParts.length - 1;
        while (patternIdxEnd >= 0 && !isDoubleWildcard(patternParts[patternIdxEnd])) {
            patternIdxEnd--;
        }

        int pathIdxStart = 0;
        for (int i = 0; i <= patternIdxEnd && pathIdxStart < pathParts.length; i++) {
            if (isDoubleWildcard(patternParts[i])) {
                break;
            }
            if (!matchStrings(patternParts[i], pathParts[pathIdxStart])) {
                break;
            }
            pathIdxStart++;
        }

        int pathIdxEnd = pathParts.length - 1;
        for (int i = patternParts.length - 1; i >= patternIdxStart && pathIdxEnd >= pathIdxStart; i--) {
            if (isDoubleWildcard(patternParts[i])) {
                break;
            }
            if (!matchStrings(patternParts[i], pathParts[pathIdxEnd])) {
                break;
            }
            pathIdxEnd--;
        }

        for (int i = pathIdxStart; i <= pathIdxEnd; i++) {
            result.add(pathParts[i]);
        }

        return result.toArray(new String[0]);
    }

    private boolean isDoubleWildcard(String str) {
        return "**".equals(str);
    }

    public String combine(String pattern1, String pattern2) {
        if (pattern1 == null && pattern2 == null) {
            return "";
        }
        if (pattern1 == null) {
            return pattern2;
        }
        if (pattern2 == null) {
            return pattern1;
        }

        if (pattern1.contains("**") || pattern2.contains("**")) {
            throw new IllegalArgumentException("Cannot combine patterns with **");
        }

        return pattern1 + pathSeparator + pattern2;
    }

    public static class AntPathComparator implements Comparator<String> {
        @Override
        public int compare(String pattern1, String pattern2) {
            AntPathMatcher matcher = new AntPathMatcher();
            int wildCount1 = countWildcards(pattern1);
            int wildCount2 = countWildcards(pattern2);
            int diff = wildCount2 - wildCount1;
            if (diff != 0) {
                return diff;
            }
            int doubleWildCount1 = countDoubleWildcards(pattern1);
            int doubleWildCount2 = countDoubleWildcards(pattern2);
            diff = doubleWildCount2 - doubleWildCount1;
            return diff;
        }

        private int countWildcards(String pattern) {
            int count = 0;
            for (char c : pattern.toCharArray()) {
                if (c == '*' || c == '?') {
                    count++;
                }
            }
            return count;
        }

        private int countDoubleWildcards(String pattern) {
            int count = 0;
            int index = 0;
            while ((index = pattern.indexOf("**", index)) >= 0) {
                count++;
                index += 2;
            }
            return count;
        }
    }
}
