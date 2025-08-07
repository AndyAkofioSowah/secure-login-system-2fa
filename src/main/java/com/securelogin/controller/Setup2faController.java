package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import com.securelogin.util.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class Setup2faController {

    private static final String ISSUER = "YourAppName";

    private final UserService userService;

    @Autowired
    public Setup2faController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/setup-2fa")
    public String showSetup2fa(Model model) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

// 1) generate & persist the TOTP secret
        String secret = OtpUtils.generateBase32Secret();
        user.setTotpSecret(secret);
        userService.updateTotpSecret(user);

        // 2) generate QR
        String otpAuthUri = OtpUtils.generateUri("YourAppName", user.getUsername(), secret);
        String qrUrl = "https://quickchart.io/qr"
                + "?text=" + URLEncoder.encode(otpAuthUri, StandardCharsets.UTF_8)
                + "&size=200"
                + "&ecLevel=L";

        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("secret", secret);
        return "setup-2fa";
    }
}




