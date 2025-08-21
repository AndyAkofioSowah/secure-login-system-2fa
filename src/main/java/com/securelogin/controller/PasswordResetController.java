package com.securelogin.controller;

import com.securelogin.model.PasswordResetToken;
import com.securelogin.service.MailService;
import com.securelogin.service.PasswordResetService;
import com.securelogin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordResetController {

    private final UserService userService;
    private final PasswordResetService resetService;
    private final MailService mail;

    public PasswordResetController(UserService userService, PasswordResetService resetService, MailService mail) {
        this.userService = userService;
        this.resetService = resetService;
        this.mail = mail;
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam("token") String token, Model model) {
        try {
            PasswordResetToken prt = resetService.requireValidToken(token);
            model.addAttribute("token", prt.getToken());
            return "reset-password";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password-invalid";
        }
    }

    @PostMapping("/reset-password")
    public String performReset(@RequestParam("token") String token,
                               @RequestParam("password") String password,
                               @RequestParam("confirm") String confirm,
                               Model model) {
        if (!password.equals(confirm)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Passwords do not match.");
            return "reset-password";
        }

        try {
            var prt = resetService.requireValidToken(token);
            var user = prt.getUser();
            userService.updatePassword(user, password);
            resetService.markUsed(prt);
            return "redirect:/login?reset=success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password-invalid";
        }
    }
}

