package com.healthfirst.pims.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** SHA-256 helper so passwords are not stored as plaintext in the coursework database. */
public final class PasswordUtil {
    private PasswordUtil() { }

    public static String sha256(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : digest) hex.append(String.format("%02x", value));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available in this Java installation.", e);
        }
    }
}
