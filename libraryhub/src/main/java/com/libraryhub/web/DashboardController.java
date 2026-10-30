package com.libraryhub.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.libraryhub.model.Role;
import com.libraryhub.model.User;
import com.libraryhub.repository.UserRepository;
import com.libraryhub.service.DashboardService;
import com.libraryhub.service.LibraryUserDetails;
import com.libraryhub.util.SecurityUtil;

@Controller
public class DashboardController {

    private final DashboardService dashboard;
    private final UserRepository users;

    public DashboardController(DashboardService dashboard, UserRepository users) {
        this.dashboard = dashboard;
        this.users = users;
    }

    @GetMapping("/dashboard")
    public String memberDashboard(Authentication authentication, Model model) {
        LibraryUserDetails principal = SecurityUtil.principal(authentication)
                .orElseThrow(() -> new AccessDeniedException("Sign in to see your dashboard"));
        if (principal.getRole() == Role.ADMIN) {
            return "redirect:/admin";
        }
        User user = users.findById(principal.getId())
                .orElseThrow(() -> new AccessDeniedException("This account no longer exists"));
        model.addAttribute("firstName", user.firstName());
        model.addAttribute("dashboard", dashboard.userDashboard(user.id()));
        return "user/dashboard";
    }
}
