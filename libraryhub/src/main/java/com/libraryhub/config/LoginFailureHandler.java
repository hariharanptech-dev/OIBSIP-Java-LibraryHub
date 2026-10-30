package com.libraryhub.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.libraryhub.service.LoginAttemptService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Counts the failure and sends the person back to the form with one generic message. */
public class LoginFailureHandler implements AuthenticationFailureHandler {

    /** Session key that carries the typed email to the next sign-in page view. */
    public static final String LAST_EMAIL_ATTRIBUTE = "LIBRARYHUB_LAST_EMAIL";

    private final LoginAttemptService attempts;

    public LoginFailureHandler(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("email");
        attempts.recordFailure(email);
        String typed = email == null ? "" : email.trim();
        request.getSession().setAttribute(LAST_EMAIL_ATTRIBUTE, typed.length() > 120 ? "" : typed);
        response.sendRedirect(request.getContextPath() + "/login?error");
    }
}
