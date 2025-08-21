package com.securelogin.service;

import com.securelogin.model.User;
import com.securelogin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Optional<User> findOptionalByEmail(String email) {
        return repo.findByEmail(email);
    }

    // For NEW users only — encode once here
    public User createUser(String username, String email, String rawPassword, String totpSecret) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setTotpSecret(totpSecret);
        return repo.save(u);
    }

    // Update only the TOTP secret
    public void updateTotpSecret(User user, String secret) {
        user.setTotpSecret(secret);
        repo.save(user);
    }

    // Change password (encode once)
    @Transactional
    public void updatePassword(User user, String rawPassword) {
        user.setPassword(encoder.encode(rawPassword));
        repo.save(user);
    }

    public boolean emailExists(String email) {
        return repo.existsByEmailIgnoreCase(email);
    }

}



