package com.libraryhub.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.libraryhub.service.DashboardService;

@Controller
public class AdminDashboardController {

    private final DashboardService dashboard;

    public AdminDashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboard.adminStats());
        return "admin/dashboard";
    }
}
