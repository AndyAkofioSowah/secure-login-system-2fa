package com.securelogin.controller;

import com.securelogin.util.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class LoginController {


    @Autowired
    private LoginAttemptService loginAttemptService;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Will render templates/login.html
    }

    @PostMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        String ip = request.getRemoteAddr();

        if (loginAttemptService.isBlocked(ip)) {
            model.addAttribute("error", "Too many failed attempts. Try again later.");
            return "login";
        }

        try {
            // Perform authentication logic
            loginAttemptService.loginSucceeded(ip);
            return "redirect:/setup-2fa";
        } catch (Exception e) {
            loginAttemptService.loginFailed(ip);
            model.addAttribute("error", "Invalid credentials.");
            return "login";
        }
    }
}
