package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import com.securelogin.util.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    public String showSetup2fa(Principal principal, Model model) throws Exception {


        User user = userService.findByUsername(principal.getName());

        // 1) generate & persist the TOTP secret
        String secret = OtpUtils.generateBase32Secret();
        user.setTotpSecret(secret);
        userService.registerUser(user);

        // 2) build the otpauth:// URI (this is what Authenticator apps expect)
        String otpAuthUri = OtpUtils.generateUri("YourAppName", user.getUsername(), secret);

// new (QuickChart.io – actively maintained):
        String qrUrl = "https://quickchart.io/qr"
                + "?text=" + URLEncoder.encode(otpAuthUri, StandardCharsets.UTF_8)
                + "&size=200"    // this yields a 200×200 px image
                + "&ecLevel=L";  // error-correction level (L/M/Q/H)

        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("secret", secret);
        return "setup-2fa";
    }





}



