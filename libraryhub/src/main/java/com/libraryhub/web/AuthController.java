package com.libraryhub.web;

import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.libraryhub.config.AppProperties;
import com.libraryhub.config.LoginFailureHandler;
import com.libraryhub.dto.RegisterForm;
import com.libraryhub.service.AuthService;
import com.libraryhub.service.DataSeeder;
import com.libraryhub.service.EmailAlreadyUsedException;
import com.libraryhub.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private static final String DUPLICATE_EMAIL =
            "An account with this email already exists. Try signing in instead.";

    private final AuthService authService;
    private final AppProperties properties;

    public AuthController(AuthService authService, AppProperties properties) {
        this.authService = authService;
        this.properties = properties;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error,
                            @RequestParam(name = "logout", required = false) String logout,
                            @RequestParam(name = "locked", required = false) String locked,
                            Authentication authentication, HttpServletRequest request, Model model) {
        if (SecurityUtil.isAuthenticated(authentication)) {
            return "redirect:" + SecurityUtil.homePathFor(authentication);
        }
        if (locked != null) {
            long seconds = parseSeconds(locked);
            model.addAttribute("loginError", "Too many sign-in attempts. Try again in " + seconds
                    + (seconds == 1 ? " second." : " seconds."));
        } else if (error != null) {
            model.addAttribute("loginError", "We could not sign you in. Check your email and password and try again. "
                    + "If you think your account is inactive, contact the library desk.");
        } else if (logout != null) {
            model.addAttribute("loginNotice", "You have been signed out.");
        }

        String lastEmail = "";
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object stored = session.getAttribute(LoginFailureHandler.LAST_EMAIL_ATTRIBUTE);
            if (stored instanceof String text) {
                lastEmail = text;
            }
            session.removeAttribute(LoginFailureHandler.LAST_EMAIL_ATTRIBUTE);
        }
        model.addAttribute("lastEmail", lastEmail);
        model.addAttribute("showDemo", properties.isShowDemoCredentials());
        model.addAttribute("demoAccounts", DataSeeder.DEMO_ACCOUNTS);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (SecurityUtil.isAuthenticated(authentication)) {
            return "redirect:" + SecurityUtil.homePathFor(authentication);
        }
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result,
                           Authentication authentication, RedirectAttributes redirect) {
        if (SecurityUtil.isAuthenticated(authentication)) {
            return "redirect:" + SecurityUtil.homePathFor(authentication);
        }
        if (!result.hasFieldErrors("password")
                && form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            result.rejectValue("password", "tooLong", "That password is too long. Use 72 characters or fewer.");
        }
        if (!result.hasFieldErrors("password") && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "The passwords don't match.");
        }
        if (!result.hasFieldErrors("email") && authService.emailTaken(form.getEmail())) {
            result.rejectValue("email", "duplicate", DUPLICATE_EMAIL);
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(form.getName(), form.getEmail(), form.getPassword());
        } catch (EmailAlreadyUsedException ex) {
            result.rejectValue("email", "duplicate", DUPLICATE_EMAIL);
            return "auth/register";
        }
        redirect.addFlashAttribute("toast", Toast.success("Your account is ready. Sign in to continue."));
        return "redirect:/login";
    }

    private static long parseSeconds(String raw) {
        try {
            long value = Long.parseLong(raw.trim());
            return Math.min(Math.max(value, 1), 3600);
        } catch (NumberFormatException ex) {
            return 60;
        }
    }
}
