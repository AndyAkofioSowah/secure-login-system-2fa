package com.securelogin.repository;

import com.securelogin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // safer for forgot-password (won’t throw on dupes)
    Optional<User> findFirstByEmailIgnoreCase(String email);

    // helpful for validation / cleanup
    boolean existsByEmailIgnoreCase(String email);

}
