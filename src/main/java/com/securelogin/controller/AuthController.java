package com.securelogin.controller;

import com.securelogin.model.User;
import com.securelogin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @ResponseBody
    public String register(@RequestBody User user) {
        userService.registerUser(user);
        return "User registered successfully!";
    }


}
