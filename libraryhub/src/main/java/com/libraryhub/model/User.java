package com.libraryhub.model;

import java.time.LocalDateTime;
import java.util.Locale;

public record User(
        long id,
        String name,
        String email,
        String passwordHash,
        Role role,
        boolean active,
        LocalDateTime createdAt) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /** First word of the name, used in greetings. */
    public String firstName() {
        String trimmed = name == null ? "" : name.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
    }

    /** Up to two capital letters for avatar badges. */
    public String initials() {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }
        String[] parts = trimmed.split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.ROOT);
    }
}
