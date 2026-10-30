package com.libraryhub.util;

import java.util.Locale;

/** ISBN-10 and ISBN-13 validation with check digits. Hyphens and spaces are ignored. */
public final class IsbnValidator {

    private IsbnValidator() {
    }

    /** Removes hyphens and spaces and upper-cases a trailing X. Null becomes an empty string. */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    public static boolean isValid(String raw) {
        String isbn = normalize(raw);
        return switch (isbn.length()) {
            case 10 -> isValidIsbn10(isbn);
            case 13 -> isValidIsbn13(isbn);
            default -> false;
        };
    }

    private static boolean isValidIsbn10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);
            int value;
            if (c == 'X' && i == 9) {
                value = 10;
            } else if (c >= '0' && c <= '9') {
                value = c - '0';
            } else {
                return false;
            }
            sum += (10 - i) * value;
        }
        return sum % 11 == 0;
    }

    private static boolean isValidIsbn13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            char c = isbn.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
            sum += (c - '0') * (i % 2 == 0 ? 1 : 3);
        }
        return sum % 10 == 0;
    }
}
