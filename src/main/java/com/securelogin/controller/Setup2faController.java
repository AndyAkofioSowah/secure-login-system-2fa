package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import com.securelogin.util.OtpUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // Setup2faController.java
    @GetMapping("/setup-2fa")
    public String showSetup2fa(
            @RequestParam(name = "reset", required = false, defaultValue = "false") boolean reset,
            HttpSession session,
            Model model) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        var user = userService.findByUsername(auth.getName());

        // If 2FA already enabled and not resetting, send them to verify
        String existing = user.getTotpSecret();
        Boolean verified = (Boolean) session.getAttribute("is2faVerified");
        if (!reset && existing != null && !existing.isBlank() && Boolean.TRUE.equals(verified)) {
            return "redirect:/verify-2fa";
        }

        // If resetting, clear any in-progress setup
        if (reset) {
            session.removeAttribute("PENDING_TOTP");
        }

        // Use a session-scoped pending secret during setup/reset
        String secret = (String) session.getAttribute("PENDING_TOTP");
        if (secret == null) {
            secret = OtpUtils.generateBase32Secret();
            session.setAttribute("PENDING_TOTP", secret);
        }

        String uri = OtpUtils.generateUri("YourAppName", user.getUsername(), secret);
        String qrUrl = "https://quickchart.io/qr?text="
                + URLEncoder.encode(uri, StandardCharsets.UTF_8)
                + "&size=200&ecLevel=L";

        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("secret", secret);
        return "setup-2fa";
    }


    @PostMapping("/setup-2fa")
    public String confirmSetup(
            @RequestParam("code") String code,
            HttpSession session,
            Model model) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        var user = userService.findByUsername(auth.getName());
        String secret = (String) session.getAttribute("PENDING_TOTP");

        if (secret == null) {
            model.addAttribute("error", "Setup session expired. Please try again.");
            return "setup-2fa";
        }

        if (OtpUtils.isCodeValid(secret, code)) {
            // ✅ Save secret to DB
            userService.updateTotpSecret(user, secret);

            // ✅ Clear session flag
            session.removeAttribute("PENDING_TOTP");
            session.setAttribute("is2faSetupPending", false);
            session.setAttribute("is2faVerified", true);

            return "redirect:/home";
        }
        else {
            model.addAttribute("error", "Invalid code. Try again.");
            model.addAttribute("qrUrl", "https://quickchart.io/qr?text="
                    + URLEncoder.encode(OtpUtils.generateUri(ISSUER, user.getUsername(), secret), StandardCharsets.UTF_8)
                    + "&size=200&ecLevel=L");
            model.addAttribute("secret", secret);
            return "setup-2fa";
        }
    }


}




