package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String showHome(Principal principal) {
        User u = userService.findByUsername(principal.getName());
        if (u.getTotpSecret() == null) {
            // they’ve never scanned a QR code yet
            return "redirect:/setup-2fa";
        }
        // they’ve already set up 2FA, so show them your “real” home page
        return "home";
    }
}

