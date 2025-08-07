package com.securelogin.controller;

import com.securelogin.util.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    private LoginAttemptService loginAttemptService;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        Object error = request.getSession().getAttribute("error");
        if (error != null) {
            model.addAttribute("error", error);
            request.getSession().removeAttribute("error");
        }
        return "login";
    }
}
