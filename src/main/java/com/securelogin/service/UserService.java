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
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }


}

