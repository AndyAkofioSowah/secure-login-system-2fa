package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import com.securelogin.util.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class Setup2faController {

    private static final String ISSUER = "YourAppName";

    private final UserService userService;

    @Autowired
    public Setup2faController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/setup-2fa")
    public String showSetup2fa(Principal principal, Model model) {
        // 1) load the user who just registered
        User user = userService.findByUsername(principal.getName());

        // 2) generate & store a fresh Base32 secret
        String secret = OtpUtils.generateBase32Secret();
        user.setTotpSecret(secret);
        userService.registerUser(user);

        // 3) build the otpauth URL for the authenticator app
        String otpAuthUrl = OtpUtils.buildOtpAuthUrl(
                ISSUER,
                user.getUsername(),
                secret
        );

        model.addAttribute("qrUrl", otpAuthUrl);
        return "setup-2fa";
    }
}



