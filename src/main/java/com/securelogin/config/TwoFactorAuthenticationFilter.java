package com.securelogin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TwoFactorAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1) always let through any static resource:
        if (path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2) if not logged in, just continue
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }

        // 3) allow the 2FA endpoints themselves
        if (path.startsWith("/setup-2fa")
                || path.startsWith("/verify-2fa")
                || path.startsWith("/logout")) {
            chain.doFilter(request, response);
            return;
        }

        // 4) now we’re logged-in and not hitting a public/static/2fa URL —
        //    check if they’ve completed 2FA
        Boolean passed2fa = (Boolean) request.getSession()
                .getAttribute("is2faVerified");
        if (Boolean.TRUE.equals(passed2fa)) {
            chain.doFilter(request, response);
        } else {
            // not yet done → send them to the verify page
            response.sendRedirect(request.getContextPath() + "/verify-2fa");
        }
    }
}

