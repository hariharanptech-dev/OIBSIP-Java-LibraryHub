package com.libraryhub.config;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.libraryhub.service.LoginAttemptService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Turns away sign-in attempts for an email that is currently locked, before any password check. */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService attempts;

    public LoginRateLimitFilter(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String email = request.getParameter("email");
        if (email != null && attempts.isLocked(email)) {
            long seconds = Math.max(1, attempts.secondsRemaining(email));
            response.sendRedirect(request.getContextPath() + "/login?locked=" + seconds);
            return;
        }
        chain.doFilter(request, response);
    }
}
