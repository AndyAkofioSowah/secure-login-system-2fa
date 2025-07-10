package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import com.securelogin.util.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        if (userService.userExists(user.getUsername())) {
            model.addAttribute("error", "Username already taken");
            return "register";
        }

        // 1) generate a fresh Base32 2FA secret
        String secret = OtpUtils.generateBase32Secret();
        user.setTotpSecret(secret);

        // 2) save the user (with hashed password etc.)
        userService.registerUser(user);

        // 3) redirect into your setup-2fa flow
        return "redirect:/setup-2fa";
    }
}


