package com.reservation.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Password hashing helper (SHA-256, lower-case hex). */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Computes the SHA-256 digest of the given text.
     *
     * @param plainText the text to hash (never null)
     * @return the 64-character lower-case hexadecimal digest
     */
    public static String sha256(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the Java specification, so this cannot happen.
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
