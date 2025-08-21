package com.securelogin.repository;

import com.securelogin.model.PasswordResetToken;
import com.securelogin.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // simple and clear: delete by the User entity
    void deleteByUser(User user);

    // (optional helper for cleanup jobs)
    long deleteByExpiresAtBefore(Instant instant);

    @Transactional
    @Modifying
    long deleteAllByExpiresAtBefore(Instant now); //
}

