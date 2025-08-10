package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import com.securelogin.util.OtpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
                               HttpServletRequest request) {
        if (userService.userExists(form.getUsername())) {
            request.getSession().setAttribute("error", "Username already taken");
            return "register";
        }

        String raw = form.getPassword();
        String secret = OtpUtils.generateBase32Secret();

        // persist new user (single encode happens inside service)
        User saved = userService.createUser(form.getUsername(), form.getEmail(), raw, secret);

        // auto-login so /setup-2fa loads
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(saved.getUsername(), raw));
        SecurityContextHolder.getContext().setAuthentication(auth);
        loginAttemptService.loginSucceeded(request.getRemoteAddr());

        return "redirect:/setup-2fa";
    }

}


