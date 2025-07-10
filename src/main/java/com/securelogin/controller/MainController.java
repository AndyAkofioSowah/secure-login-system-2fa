package com.securelogin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String showLandingPage() {
        return "index"; // maps to index.html
    }

    @GetMapping("/home")
    public String showUserHome() {
        return "home"; // logged-in users redirected here after login
    }
}
