package com.libraryhub.web;

import java.io.Serializable;

/** A short message shown once after a redirect. Types: success, info, warning, error. */
public record Toast(String type, String message) implements Serializable {

    public static Toast success(String message) {
        return new Toast("success", message);
    }

    public static Toast info(String message) {
        return new Toast("info", message);
    }

    public static Toast warning(String message) {
        return new Toast("warning", message);
    }

    public static Toast error(String message) {
        return new Toast("error", message);
    }

    /** Icon name from the sprite. Every toast carries an icon as well as text. */
    public String icon() {
        return switch (type) {
            case "success" -> "check-circle";
            case "warning" -> "alert";
            case "error" -> "x-circle";
            default -> "info";
        };
    }
}
