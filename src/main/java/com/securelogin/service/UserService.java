package com.securelogin.service;

import com.securelogin.model.User;
import com.securelogin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public boolean userExists(String username) {
        return repo.findByUsername(username).isPresent();
    }

    public User findByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    // NEW users only — always encode here
    public User createUser(String username, String email, String rawPassword, String totpSecret) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));  // single encode
        u.setTotpSecret(totpSecret);
        return repo.save(u);
    }

    // Update secret only — never touch password
    public void updateTotpSecret(User user, String secret) {
        user.setTotpSecret(secret);
        repo.save(user);
    }
}


