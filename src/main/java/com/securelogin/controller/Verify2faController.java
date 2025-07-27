package com.securelogin.controller;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.securelogin.model.User;
import com.securelogin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import java.security.Principal;
import java.time.Instant;

@Controller
public class Verify2faController {

    private final UserService userService;
    private final TimeBasedOneTimePasswordGenerator totpGenerator =
            new TimeBasedOneTimePasswordGenerator();

    @Autowired
    public Verify2faController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify-2fa")
    public String showVerifyForm() {
        return "verify-2fa";
    }

    @PostMapping("/verify-2fa")
    public String verify2fa(@RequestParam("code") int code,
                            Principal principal,
                            RedirectAttributes redirect,
                            HttpServletRequest request) throws Exception {

        User user = userService.findByUsername(principal.getName());

        // decode the Base32 secret into raw bytes
        Base32 base32 = new Base32();
        byte[] secretBytes = base32.decode(user.getTotpSecret());

        // wrap as an HMAC-SHA1 key (that’s what the default TOTP spec uses)
        SecretKey key = new SecretKeySpec(secretBytes, "HmacSHA1");

        // generate the 6-digit code for *right now*
        Instant now = Instant.now();
        int validCode = totpGenerator.generateOneTimePassword(key, now);

        if (code != validCode) {
            redirect.addFlashAttribute("error", "Invalid 2FA code");
            return "redirect:/verify-2fa";
        }
    //if verified =
        request.getSession().setAttribute("is2faVerified", true);
        return "redirect:/home";

    }


}

