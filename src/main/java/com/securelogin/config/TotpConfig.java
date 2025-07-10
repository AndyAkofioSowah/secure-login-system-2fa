package com.securelogin.config;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;

@Configuration
public class TotpConfig {
    @Bean
    public TimeBasedOneTimePasswordGenerator totpGenerator() throws NoSuchAlgorithmException {
        return new TimeBasedOneTimePasswordGenerator();
    }
}

