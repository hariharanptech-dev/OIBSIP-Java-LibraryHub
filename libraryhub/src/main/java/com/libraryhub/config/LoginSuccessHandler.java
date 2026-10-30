package com.libraryhub.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.libraryhub.service.LoginAttemptService;
import com.libraryhub.util.SecurityUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Clears the failure count, then returns the person to the page they asked for before signing in,
 * or to their role's home page when there was none.
 */
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final LoginAttemptService attempts;
    private final RequestCache requestCache;

    public LoginSuccessHandler(LoginAttemptService attempts, RequestCache requestCache) {
        this.attempts = attempts;
        this.requestCache = requestCache;
        setRequestCache(requestCache);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        attempts.recordSuccess(authentication.getName());
        SavedRequest saved = requestCache.getRequest(request, response);
        if (saved == null) {
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, SecurityUtil.homePathFor(authentication));
            return;
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
