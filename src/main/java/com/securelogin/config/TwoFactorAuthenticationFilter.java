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

        String path = request.getServletPath();

        // 1) Static assets: always allow
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) {
            chain.doFilter(request, response);
            return;
        }

        // Extras browsers often hit
        if (path.equals("/favicon.ico") || path.equals("/error")) {
            chain.doFilter(request, response);
            return;
        }

        // 2) If not logged in, don't enforce 2FA
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }

        // 3) Always allow core auth/reset/setup/verify/logout endpoints
        if (path.startsWith("/setup-2fa")
                || path.startsWith("/verify-2fa")
                || path.startsWith("/logout")
                || path.startsWith("/forgot-password")
                || path.startsWith("/reset-password")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")) {
            chain.doFilter(request, response);
            return;
        }


        // 4) Check session attributes
        Boolean setupPending = (Boolean) request.getSession().getAttribute("is2faSetupPending");
        Boolean passed2fa    = (Boolean) request.getSession().getAttribute("is2faVerified");

        if (Boolean.TRUE.equals(setupPending)) {
            // User just registered and needs to finish setup → force them to /setup-2fa
            response.sendRedirect(request.getContextPath() + "/setup-2fa");
            return;
        }

        if (Boolean.TRUE.equals(passed2fa)) {
            // Normal flow: user has passed 2FA → allow
            chain.doFilter(request, response);
        } else {
            // Logged in but not verified yet → send to verify
            response.sendRedirect(request.getContextPath() + "/verify-2fa");
        }
    }
}
