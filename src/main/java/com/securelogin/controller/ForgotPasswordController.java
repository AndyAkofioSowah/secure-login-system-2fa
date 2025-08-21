package com.securelogin.controller;

import com.securelogin.model.PasswordResetToken;
import com.securelogin.model.User;
import com.securelogin.repository.PasswordResetTokenRepository;
import com.securelogin.repository.UserRepository;
import com.securelogin.service.MailService;
import com.securelogin.util.PasswordResetRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Controller
public class ForgotPasswordController {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final MailService mailService;
    private final PasswordResetRateLimiter limiter;

    @Value("${app.reset-token.minutes:30}")
    private int tokenMinutes;

    @Value("${app.show-dev-reset-link:false}")
    private boolean showDevLink;

    private static final SecureRandom RNG = new SecureRandom();

    public ForgotPasswordController(UserRepository userRepo,
                                    PasswordResetTokenRepository tokenRepo,
                                    MailService mailService,
                                    PasswordResetRateLimiter limiter) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.mailService = mailService;
        this.limiter = limiter;
    }

    @GetMapping("/forgot-password")
    public String view() {
        return "forgot-password";
    }


    @PostMapping("/forgot-password")
    @Transactional
    public String submit(@RequestParam String email,
                         HttpServletRequest request,
                         Model model) {
        // normalize email for lookup + rate limit key
        String normalizedEmail = (email == null) ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);

        // rate-limit on IP+email
        String key = request.getRemoteAddr() + "|" + normalizedEmail;
        if (!limiter.allow(key)) {
            // neutral response even if rate-limited
            return showDevLink ? "forgot-password-sent" : "redirect:/forgot-password/sent";
        }

        // Safe lookup: won’t throw if duplicates exist
        userRepo.findFirstByEmailIgnoreCase(normalizedEmail).ifPresent(user -> {
            // Invalidate previous tokens for this user
            tokenRepo.deleteByUser(user);

            // Create new token
            String token = generateToken();
            var t = new PasswordResetToken();
            t.setToken(token);
            t.setUser(user);
            t.setExpiresAt(Instant.now().plus(tokenMinutes, ChronoUnit.MINUTES));
            tokenRepo.save(t);

            // Build absolute URL
            String resetUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/reset-password")
                    .queryParam("token", token)
                    .toUriString();

            // Send mail (don’t leak errors to user)
            try {
                mailService.sendPasswordReset(user.getEmail(), resetUrl, tokenMinutes);
            } catch (Exception ignored) { /* log if you want */ }

            // In dev, show the link on the confirmation page
            if (showDevLink) {
                model.addAttribute("devLink", resetUrl);
            }
        });

        // Always show neutral page (no user enumeration)
        return showDevLink ? "forgot-password-sent" : "redirect:/forgot-password/sent";
    }


    @GetMapping("/forgot-password/sent")
    public String sent() {
        return "forgot-password-sent";
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
