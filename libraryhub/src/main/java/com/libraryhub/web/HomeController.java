package com.libraryhub.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.libraryhub.service.DashboardService;

@Controller
public class HomeController {

    private final DashboardService dashboard;

    public HomeController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totals", dashboard.libraryTotals());
        return "home";
    }
}
