package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import com.securelogin.util.OtpUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Verify2faController {

    private final UserService userService;

    @Autowired
    public Verify2faController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify-2fa")
    public String showVerifyPage() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        return "verify-2fa";
    }

    @PostMapping("/verify-2fa")
    public String verifyCode(
            @RequestParam("code") String code,
            HttpSession session,
            Model model) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName());

        if (OtpUtils.isCodeValid(user.getTotpSecret(), code)) {
            session.setAttribute("is2faVerified", true);
            return "redirect:/dashboard"; // 🎯 success
        } else {
            model.addAttribute("error", "Invalid code, try again.");
            return "verify-2fa";
        }
    }
}


