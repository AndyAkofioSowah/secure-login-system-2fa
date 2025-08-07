package com.securelogin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import com.securelogin.service.CustomUserDetailsService;

@Component
public class DebugAuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    public DebugAuthenticationProvider(CustomUserDetailsService userDetailsService,
                                       BCryptPasswordEncoder passwordEncoder) {
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
    }

    @Override
    protected void additionalAuthenticationChecks(
            UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication
    ) throws AuthenticationException {
        String raw = authentication.getCredentials().toString();
        String hash = userDetails.getPassword();
        boolean matches = getPasswordEncoder().matches(raw, hash);
        System.out.printf("🔍 DEBUG AUTH: raw=\"%s\", hash=\"%s\", matches=%s%n", raw, hash, matches);

        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
