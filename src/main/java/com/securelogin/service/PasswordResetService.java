package com.securelogin.service;

import com.securelogin.model.PasswordResetToken;
import com.securelogin.model.User;
import com.securelogin.repository.PasswordResetTokenRepository;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository tokens;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository tokens) {
        this.tokens = tokens;
    }

    public String createToken(User user) {
        // Remove old tokens for this user (optional but cleaner)
        tokens.deleteByUser(user);

        // URL-safe random token
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.encodeBase64URLSafeString(bytes);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUser(user);
        prt.setExpiresAt(Instant.now().plus(Duration.ofMinutes(30)));
        tokens.save(prt);

        return token;
    }

    public PasswordResetToken requireValidToken(String token) {
        var prt = tokens.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (prt.isUsed()) throw new IllegalStateException("Token already used");
        if (prt.isExpired()) throw new IllegalStateException("Token expired");
        return prt;
    }

    public void markUsed(PasswordResetToken prt) {
        prt.setUsedAt(Instant.now());
        tokens.save(prt);
    }
}
