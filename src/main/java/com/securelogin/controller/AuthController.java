package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import com.securelogin.util.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (userService.userExists(user.getUsername())) {
            return "Username already taken";
        }

        String secret = OtpUtils.generateBase32Secret();
        userService.createUser(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(), // RAW password; service will encode
                secret
        );

        return "User registered successfully!";
    }
}

