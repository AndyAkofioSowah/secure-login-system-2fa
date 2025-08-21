package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final UserService userService;

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private com.securelogin.util.LoginAttemptService loginAttemptService;

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
    public String registerUser(@ModelAttribute("user") User form,
                               Model model,
                               HttpServletRequest request) {
        String username = form.getUsername().trim();
        String email    = form.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        String raw      = form.getPassword();

        try {
            // Generate TOTP secret right away
            String secret = com.securelogin.util.OtpUtils.generateBase32Secret();
            User saved = userService.createUser(username, email, raw, secret);

            // Auto-login so /setup-2fa loads
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, raw));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Mark 2FA status in session
            request.getSession().setAttribute("is2faVerified", false);
            request.getSession().setAttribute("is2faSetupPending", true); // NEW 👈

            loginAttemptService.loginSucceeded(request.getRemoteAddr());
            return "redirect:/setup-2fa";

        } catch (DataIntegrityViolationException e) {
            // Check which field caused the conflict
            if (userService.userExists(username)) {
                model.addAttribute("error", "Username already taken");
            } else if (userService.emailExists(email)) {
                model.addAttribute("error", "Email already in use");
            } else {
                model.addAttribute("error", "Registration failed. Please try again.");
            }
            return "register";
        }
    }




}


