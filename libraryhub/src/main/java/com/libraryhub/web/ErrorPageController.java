package com.libraryhub.web;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Renders one styled error page for every failure. Stack traces and exception text never reach the page. */
@Controller
public class ErrorPageController implements ErrorController {

    @RequestMapping("/error")
    public String handle(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = attribute instanceof Integer code ? code : 500;

        String headline;
        String message;
        switch (status) {
            case 403 -> {
                headline = "You don't have access to this page";
                message = "Your account doesn't have permission to open it, or your session has expired. "
                        + "Go back and try again, or sign in with a different account.";
            }
            case 404 -> {
                headline = "We couldn't find that page";
                message = "The link may be old or mistyped. Head back to the home page and start from there.";
            }
            default -> {
                if (status >= 500) {
                    headline = "Something went wrong on our side";
                    message = "The problem has been logged. Please try again in a moment.";
                } else {
                    headline = "That request didn't go through";
                    message = "Please go back and try again.";
                }
            }
        }
        response.setStatus(status);
        model.addAttribute("status", status);
        model.addAttribute("headline", headline);
        model.addAttribute("message", message);
        return "error/page";
    }
}
