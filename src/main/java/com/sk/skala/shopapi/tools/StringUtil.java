package com.sk.skala.shopapi.tools;

public final class StringUtil {
    private StringUtil() {
    }

    public static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isAnyEmpty(String... values) {
        if (values == null) {
            return true;
        }
        for (String value : values) {
            if (isEmpty(value)) {
                return true;
            }
        }
        return false;
    }
}
