package com.libraryhub.dto;

import java.util.Locale;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registration form. Each field has one rule and one message, so a field shows at most one error. */
public class RegisterForm {

    @Size(min = 2, max = 80, message = "Enter your full name (2 to 80 characters).")
    private String name = "";

    @Pattern(regexp = "^(?=.{3,120}$)[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$",
            message = "Enter a valid email address, like name@example.com.")
    private String email = "";

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
            message = "Use 8 to 72 characters with at least one letter and one number.")
    private String password = "";

    private String confirmPassword = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? "" : password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword == null ? "" : confirmPassword;
    }
}
