package com.securelogin.jobs;

import com.securelogin.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PasswordResetCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetCleanupJob.class);

    private final PasswordResetTokenRepository repo;

    public PasswordResetCleanupJob(PasswordResetTokenRepository repo) {
        this.repo = repo;
    }

    // Top of every hour
    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpired() {
        try {
            long removed = repo.deleteAllByExpiresAtBefore(Instant.now());
            log.debug("Password reset cleanup removed {} expired tokens", removed);
        } catch (Exception ex) {
            log.warn("Password reset cleanup job failed", ex);
        }
    }
}
