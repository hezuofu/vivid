package org.vividframework.util;

/**
 * Assertion utility for validation
 * @author sketch
 */
public final class Assert {

    private Assert() {
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object obj) {
        notNull(obj, "Object must not be null");
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean expression) {
        isTrue(expression, "Expression must be true");
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isFalse(boolean expression) {
        isFalse(expression, "Expression must be false");
    }

    public static void isEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isEmpty(String str) {
        isEmpty(str, "String must not be empty");
    }

    public static void notEmpty(String str, String message) {
        if (str != null && !str.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(String str) {
        notEmpty(str, "String must be empty");
    }

    public static void doesNotContain(String str, String searchStr, String message) {
        if (str != null && searchStr != null && str.contains(searchStr)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void doesNotContain(String str, String searchStr) {
        doesNotContain(str, searchStr, "String must not contain '" + searchStr + "'");
    }

    public static void matches(String str, String pattern, String message) {
        if (str == null || !str.matches(pattern)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void matches(String str, String pattern) {
        matches(str, pattern, "String '" + str + "' does not match pattern '" + pattern + "'");
    }

    public static void isInstanceOf(Class<?> type, Object obj, String message) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isInstanceOf(Class<?> type, Object obj) {
        isInstanceOf(type, obj, "Object of type [" + (obj != null ? obj.getClass().getName() : "null") +
                "] must be an instance of [" + type.getName() + "]");
    }

    public static void isAssignable(Class<?> superType, Class<?> subType, String message) {
        notNull(superType, "Super type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isAssignable(Class<?> superType, Class<?> subType) {
        isAssignable(superType, subType, superType.getName() + " is not assignable from " +
                (subType != null ? subType.getName() : "null"));
    }

    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static void state(boolean expression) {
        state(expression, "State must be true");
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }
}
